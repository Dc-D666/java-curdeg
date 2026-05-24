
package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.configs.AiSearchConfig;
import cn.edu.sdu.java.server.configs.ModerationConfig;
import cn.edu.sdu.java.server.models.BbsComment;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsCommentRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiSearchService {

    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\x{1F000}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{FE0F}\\x{200D}]");
    private static final Pattern UNSUPPORTED_SYMBOL_PATTERN = Pattern.compile("[✓✗★☆✔✘]");

    private static final String SYSTEM_PROMPT = """
你是校园论坛智能助手。请根据下面的帖子内容回答用户问题。

【相关帖子】
{postsContent}

【用户问题】
{question}

【回答要求】
1. 先总结帖子中的相关讨论内容，再给出针对性建议。
2. 如果没有相关帖子，请明确说明，然后基于通用知识给出一般建议。
3. 不要编造帖子中不存在的信息。
4. 可以使用受支持的 Markdown：# 标题、**加粗**、*斜体*、~~删除线~~、普通列表。
5. 重要关键词或重点内容必须使用 [红色]重点文字[/红色] 标记。
6. 禁止输出任何 emoji 表情符号。
7. 禁止输出特殊装饰符号，例如对勾、叉号、星号、图钉、灯泡、文档、禁止、警告、靶心等图标。
8. 如需强调，请使用文字前缀，例如“注意：”“重要提示：”“建议：”。
9. 段落之间用换行分隔，保持简洁友好。

【发帖建议】
如果适合引导用户发帖，请在回答结尾单独添加以下结构，结构本身不要使用 emoji：
[SUGGEST_POST]
标题：{推荐的帖子标题}
内容：{推荐的帖子初始内容}
[/SUGGEST_POST]
""";
 

    private static final String KEYWORD_REFACTOR_PROMPT = """
你是校园论坛搜索关键词优化助手。请分析用户问题，提取最核心的搜索关键词。

【用户问题】
{question}

【要求】
1. 只提取 {keywordCount} 个以内的核心关键词。
2. 关键词要简洁，优先使用用户问题中的核心词汇。
3. 多个关键词用英文逗号分隔。
4. 只输出关键词列表，不要解释。
5. 禁止输出 emoji 或特殊装饰符号。
输出格式：关键词1, 关键词2
""";

    private final BbsPostService bbsPostService;
    private final BbsCommentRepository bbsCommentRepository;
    private final RestTemplate restTemplate;
    private final ModerationConfig moderationConfig;
    private final AiSearchConfig aiSearchConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final UserRepository userRepository;
    private final LevelPrivilegeService levelPrivilegeService;

    public AiSearchService(BbsPostService bbsPostService, BbsCommentRepository bbsCommentRepository,
                          RestTemplate restTemplate, ModerationConfig moderationConfig, 
                          AiSearchConfig aiSearchConfig, ObjectMapper objectMapper,
                          UserRepository userRepository, LevelPrivilegeService levelPrivilegeService) {
        this.bbsPostService = bbsPostService;
        this.bbsCommentRepository = bbsCommentRepository;
        this.restTemplate = restTemplate;
        this.moderationConfig = moderationConfig;
        this.aiSearchConfig = aiSearchConfig;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.userRepository = userRepository;
        this.levelPrivilegeService = levelPrivilegeService;
    }

    public static class PostWithComments {
        private BbsPost post;
        private List<BbsComment> comments;

        public PostWithComments(BbsPost post, List<BbsComment> comments) {
            this.post = post;
            this.comments = comments;
        }

        public BbsPost getPost() {
            return post;
        }

        public void setPost(BbsPost post) {
            this.post = post;
        }

        public List<BbsComment> getComments() {
            return comments;
        }

        public void setComments(List<BbsComment> comments) {
            this.comments = comments;
        }
    }

    /**
     * 搜索帖子和评论（供流式接口使用）- 增强版本，支持关键词重构
     */
    public List<BbsPost> searchPostsOnly(String keyword) {
        log.debug("开始增强版搜索帖子，原始关键词: {}", keyword);
        
        // 检查配置是否启用关键词重构
        if (!aiSearchConfig.isEnabled()) {
            log.debug("关键词重构功能未启用，使用原始搜索流程");
            return searchWithSingleKeyword(keyword);
        }
        
        try {
            // 调用关键词重构
            List<String> refactoredKeywords = refactorKeywords(keyword);
            log.debug("重构后的关键词列表: {}", refactoredKeywords);
            
            if (refactoredKeywords.isEmpty()) {
                log.warn("未获得有效重构关键词，回退到原始搜索");
                return searchWithSingleKeyword(keyword);
            }
            
            // 使用重构后的关键词进行多关键词搜索
            List<BbsPost> posts = searchWithMultipleKeywords(refactoredKeywords);
            log.info("增强版搜索完成，共找到 {} 个帖子", posts.size());
            return posts;
            
        } catch (Exception e) {
            log.error("关键词重构搜索流程出错，回退到原始搜索: {}", e.getMessage(), e);
            return searchWithSingleKeyword(keyword);
        }
    }
    
    /**
     * 使用单个关键词搜索（原始流程）
     */
    private List<BbsPost> searchWithSingleKeyword(String keyword) {
        log.debug("开始搜索帖子... (单关键词)");
        DataResponse searchResponse = bbsPostService.searchPosts(keyword, "fulltext", 1, aiSearchConfig.getResultCount());
        Page<BbsPost> postPage = (Page<BbsPost>) searchResponse.getData();
        List<BbsPost> posts = postPage != null ? postPage.getContent() : new ArrayList<>();
        log.debug("搜索完成，找到 {} 个相关帖子", posts.size());
        return posts;
    }
    
    /**
     * 使用多个关键词进行搜索
     */
    public List<BbsPost> searchWithMultipleKeywords(List<String> keywords) {
        log.debug("开始多关键词搜索，关键词数量: {}, 关键词列表: {}", keywords.size(), keywords);
        
        List<List<BbsPost>> allResults = new ArrayList<>();
        
        for (String keyword : keywords) {
            try {
                log.debug("正在搜索关键词: [{}]", keyword);
                DataResponse searchResponse = bbsPostService.searchPosts(keyword, "fulltext", 1, aiSearchConfig.getResultCount());
                Page<BbsPost> postPage = (Page<BbsPost>) searchResponse.getData();
                List<BbsPost> posts = postPage != null ? postPage.getContent() : new ArrayList<>();
                log.debug("关键词 [{}] 找到 {} 个帖子", keyword, posts.size());
                allResults.add(posts);
            } catch (Exception e) {
                log.error("搜索关键词 [{}] 时出错: {}", keyword, e.getMessage(), e);
            }
        }
        
        log.debug("多关键词搜索完成，开始合并去重");
        List<BbsPost> mergedPosts = mergeAndDeduplicatePosts(allResults);
        log.debug("合并去重后有 {} 个帖子，开始排序", mergedPosts.size());
        
        List<BbsPost> sortedPosts = sortPosts(mergedPosts);
        
        // 限制结果数量
        int limit = aiSearchConfig.getResultCount();
        if (sortedPosts.size() > limit) {
            sortedPosts = sortedPosts.subList(0, limit);
        }
        
        log.debug("多关键词搜索处理完成，最终返回 {} 个帖子", sortedPosts.size());
        return sortedPosts;
    }
    
    /**
     * 合并搜索结果并根据帖子ID去重
     */
    public List<BbsPost> mergeAndDeduplicatePosts(List<List<BbsPost>> results) {
        log.debug("开始合并 {} 组搜索结果", results.size());
        
        Map<Long, BbsPost> postMap = new java.util.LinkedHashMap<>();
        
        for (List<BbsPost> postList : results) {
            for (BbsPost post : postList) {
                if (!postMap.containsKey(post.getId())) {                    
                    postMap.put(post.getId(), post);
                }
            }
        }
        
        List<BbsPost> mergedPosts = new ArrayList<>(postMap.values());
        log.debug("合并去重完成，获得 {} 个唯一帖子", mergedPosts.size());
        return mergedPosts;
    }
    
    /**
     * 排序帖子（综合考虑热度、时间、点赞数等）
     */
    public List<BbsPost> sortPosts(List<BbsPost> posts) {
        log.debug("开始排序 {} 个帖子", posts.size());
        
        List<BbsPost> sortedPosts = new ArrayList<>(posts);
        
        sortedPosts.sort((p1, p2) -> {
            // 计算综合评分
            double score1 = calculatePostScore(p1);
            double score2 = calculatePostScore(p2);
            
            // 存储评分用于调试
            p1.setMatchScore(score1);
            p2.setMatchScore(score2);
            
            // 降序排列，分数高的在前
            return Double.compare(score2, score1);
        });
        
        log.debug("排序完成");
        return sortedPosts;
    }
    
    /**
     * 计算帖子综合评分
     */
    private double calculatePostScore(BbsPost post) {
        double score = 0.0;
        
        // 点赞数权重
        int likeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
        score += likeCount * 5.0;
        
        // 评论数权重
        int commentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        score += commentCount * 3.0;
        
        // 浏览数权重
        int viewCount = post.getViewCount() != null ? post.getViewCount() : 0;
        score += viewCount * 0.1;
        
        // 收藏数权重
        int favoriteCount = post.getFavoriteCount() != null ? post.getFavoriteCount() : 0;
        score += favoriteCount * 4.0;
        
        // 置顶帖子加高分
        if (Boolean.TRUE.equals(post.getIsTop())) {
            score += 1000.0;
        }
        
        // 精选帖子加分
        if (Boolean.TRUE.equals(post.getIsFeatured())) {
            score += 500.0;
        }
        
        // 考虑时间因素（新帖子加分）
        try {
            String createTime = post.getCreateTime();
            if (createTime != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date postDate = sdf.parse(createTime);
                long daysSincePost = (System.currentTimeMillis() - postDate.getTime()) / (1000 * 60 * 60 * 24);
                // 7天内的帖子每天减5分，超过30天每天减1分
                if (daysSincePost < 7) {
                    score += (7 - daysSincePost) * 50;
                } else if (daysSincePost > 30) {
                    score -= (daysSincePost - 30) * 2;
                }
            }
        } catch (Exception e) {
            log.debug("计算帖子时间分数时出错: {}", e.getMessage());
        }
        
        return score;
    }

    /**
     * 构建带评论的帖子列表
     */
    public List<PostWithComments> buildPostWithComments(List<BbsPost> posts) {
        log.debug("开始获取评论...");
        List<PostWithComments> postWithCommentsList = new ArrayList<>();
        int totalComments = 0;

        for (BbsPost post : posts) {
            List<BbsComment> comments = bbsCommentRepository.findTop5ByPostIdAndStatusOrderByLikeCountDesc(post.getId(), 1);
            log.trace("  帖子 {} (id={}) 找到 {} 条评论", post.getTitle(), post.getId(), comments.size());
            postWithCommentsList.add(new PostWithComments(post, comments));
            totalComments += comments.size();
        }
        log.debug("评论获取完成，共 {} 条", totalComments);
        return postWithCommentsList;
    }

    /**
     * 构建Prompt内容
     */
    public String buildPrompt(String keyword, List<PostWithComments> postWithCommentsList) {
        String postsContent = buildPostsContent(postWithCommentsList);
        String prompt = SYSTEM_PROMPT
                .replace("{postsContent}", postsContent)
                .replace("{question}", keyword);
        log.trace("--- 完整Prompt开始 ---\n{}\n--- 完整Prompt结束 ---", prompt);
        return prompt;
    }

    /**
     * 流式调用AI API
     */
    public void callAiApiStream(String prompt, Consumer<String> onContent, Runnable onComplete, Consumer<Exception> onError) {
        log.debug("开始流式调用AI API");
        new Thread(() -> {
            try {
                log.debug("构建请求体...");
                Map<String, Object> requestBody = buildRequestBodyStream(prompt);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(moderationConfig.getApi().getUrl()))
                        .headers("Content-Type", "application/json")
                        .headers("Authorization", "Bearer " + moderationConfig.getApi().getKey())
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                        .build();

                log.debug("发送流式HTTP请求...");
                long startTime = System.currentTimeMillis();
                HttpResponse<java.io.InputStream> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofInputStream());
                
                log.debug("开始接收流式响应... (耗时: {}ms)", System.currentTimeMillis() - startTime);
                
                java.io.InputStream inputStream = response.body();
                java.io.InputStreamReader isr = new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);
                
                boolean completed = false;
                int totalChunks = 0;
                StringBuilder lineBuffer = new StringBuilder();
                int c;
                while ((c = isr.read()) != -1) {
                    if (c == '\n') {
                        String line = lineBuffer.toString();
                        lineBuffer.setLength(0);
                        
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data.trim())) {
                                log.debug("接收到流式结束标记，共收到{}个chunk，耗时{}ms", totalChunks, System.currentTimeMillis() - startTime);
                                completed = true;
                                onComplete.run();
                                break;
                            }
                            try {
                                JsonNode root = objectMapper.readTree(data);
                                JsonNode choices = root.get("choices");
                                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                                    JsonNode delta = choices.get(0).get("delta");
                                    if (delta != null && delta.has("content")) {
                                        String content = sanitizeAiOutput(delta.get("content").asText());
                                        totalChunks++;
                                        log.trace("AI返回chunk #{}: '{}'", totalChunks, content);
                                        onContent.accept(content);
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("解析chunk失败: {}", e.getMessage());
                            }
                        }
                    } else if (c != '\r') {
                        lineBuffer.append((char) c);
                    }
                }
                
                if (!completed) {
                    log.debug("未收到结束标记，手动完成");
                    onComplete.run();
                }
                
            } catch (Exception e) {
                log.error("流式调用AI API过程中发生错误", e);
                onError.accept(e);
            }
        }).start();
    }

    private Map<String, Object> buildRequestBodyStream(String content) {
        log.debug("构建流式请求体，内容长度={}", content.length());
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", content);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", moderationConfig.getApi().getModel());
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.2);
        requestBody.put("stream", true);

        return requestBody;
    }

    public Map<String, Object> aiSearch(String keyword) {
        return aiSearch(keyword, null);
    }

    public Map<String, Object> aiSearch(String keyword, Integer userId) {
        log.info("===== 开始增强版AI智能搜索 =====");
        log.info("搜索关键词: {}", keyword);

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                int searchLimit = levelPrivilegeService.getAiSearchLimit(user.getLevel());
                if (searchLimit <= 0) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("answer", "您当前等级不具备AI搜索权限");
                    errorResult.put("relatedPosts", new ArrayList<>());
                    return errorResult;
                }
                if (!levelPrivilegeService.checkAiUsageLimit(userId, "search", searchLimit)) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("answer", "您今日AI搜索次数已达上限(" + searchLimit + "次)，请明日再来");
                    errorResult.put("relatedPosts", new ArrayList<>());
                    return errorResult;
                }
            }
        }

        try {
            // 使用增强版搜索方法获取帖子
            log.info("1/5: 开始搜索帖子... (增强模式)");
            List<BbsPost> posts = searchPostsOnly(keyword);
            log.info("2/5: 搜索完成，找到 {} 个相关帖子", posts.size());
            
            // 输出帖子详情
            for (int i = 0; i < posts.size(); i++) {
                BbsPost post = posts.get(i);
                log.info("  帖子 {}: id={}, title={}, author={}, score={}", 
                         i+1, post.getId(), post.getTitle(), post.getAuthorNickname(), 
                         post.getMatchScore() != null ? post.getMatchScore() : "N/A");
            }

            log.info("3/5: 开始获取评论...");
            List<PostWithComments> postWithCommentsList = new ArrayList<>();
            int totalComments = 0;

            for (BbsPost post : posts) {
                List<BbsComment> comments = bbsCommentRepository.findTop5ByPostIdAndStatusOrderByLikeCountDesc(post.getId(), 1);
                log.info("  帖子 {} (id={}) 找到 {} 条评论", post.getTitle(), post.getId(), comments.size());
                postWithCommentsList.add(new PostWithComments(post, comments));
                totalComments += comments.size();
            }
            log.info("4/5: 评论获取完成，共 {} 条", totalComments);

            log.info("5/5: 构建Prompt并调用AI...");
            String postsContent = buildPostsContent(postWithCommentsList);
            String prompt = SYSTEM_PROMPT
                    .replace("{postsContent}", postsContent)
                    .replace("{question}", keyword);

            log.debug("--- 完整Prompt开始 ---\n{}\n--- 完整Prompt结束 ---", prompt);

            String answer = callAiApi(prompt);

            log.info("AI返回答案: \n{}", answer);

            Map<String, Object> result = new HashMap<>();
            result.put("answer", answer);
            result.put("relatedPosts", posts);

            log.info("===== 增强版AI智能搜索完成 =====");
            return result;

        } catch (Exception e) {
            log.error("===== AI搜索过程中发生错误 =====", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("answer", "抱歉，搜索过程中发生错误，请稍后再试。");
            errorResult.put("relatedPosts", new ArrayList<>());
            return errorResult;
        }
    }

    private String buildPostsContent(List<PostWithComments> postWithCommentsList) {
        if (postWithCommentsList.isEmpty()) {
            return "无相关帖子";
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (PostWithComments pwc : postWithCommentsList) {
            sb.append("---\n");
            sb.append("帖子").append(index).append("：\n");
            sb.append("标题：").append(pwc.getPost().getTitle()).append("\n");
            String content = pwc.getPost().getContent();
            if (content != null && content.length() > 400) {
                content = content.substring(0, 400) + "...";
            }
            sb.append("内容：").append(content).append("\n");
            
            List<BbsComment> comments = pwc.getComments();
            if (comments != null && !comments.isEmpty()) {
                sb.append("评论（Top5）：\n");
                int commentIndex = 1;
                for (BbsComment comment : comments) {
                    String commentContent = comment.getContent();
                    if (commentContent != null && commentContent.length() > 100) {
                        commentContent = commentContent.substring(0, 100) + "...";
                    }
                    sb.append(commentIndex).append(". ").append(commentContent).append("\n");
                    commentIndex++;
                }
            }
            index++;
        }
        sb.append("---");
        return sb.toString();
    }

    private String callAiApi(String prompt) {
        log.info("===== 开始调用AI API =====");
        try {
            log.info("1/3: 构建请求体...");
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            // 输出请求配置
            log.info("API配置: url={}, model={}", 
                     moderationConfig.getApi().getUrl(), 
                     moderationConfig.getApi().getModel());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(moderationConfig.getApi().getKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("2/3: 发送HTTP请求到: {}", moderationConfig.getApi().getUrl());

            long startTime = System.currentTimeMillis();
            String response = restTemplate.postForObject(moderationConfig.getApi().getUrl(), request, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("3/3: 收到响应，耗时={}ms, 响应长度={}", duration, response != null ? response.length() : 0);
            log.debug("--- 完整AI响应开始 ---\n{}\n--- 完整AI响应结束 ---", response);

            String answer = parseAiResponse(response);
            
            log.info("AI API调用完成");
            return answer;

        } catch (Exception e) {
            log.error("===== 调用AI API过程中发生错误 =====", e);
            log.error("错误详情: type={}, message={}", e.getClass().getName(), e.getMessage());
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    private Map<String, Object> buildRequestBody(String content) {
        log.debug("构建请求体，内容长度={}", content.length());
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", content);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", moderationConfig.getApi().getModel());
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.2);

        try {
            log.debug("请求体JSON: {}", objectMapper.writeValueAsString(requestBody));
        } catch (Exception e) {
            log.warn("无法序列化请求体JSON", e);
        }

        return requestBody;
    }

    private String parseAiResponse(String response) {
        log.info("开始解析AI响应...");
        try {
            JsonNode root = objectMapper.readTree(response);
            log.debug("解析JSON根节点成功");
            
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                log.warn("AI返回格式异常，无choices字段：{}", response);
                return "抱歉，AI返回格式异常。";
            }
            log.debug("找到choices数组，长度={}", choices.size());

            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message == null) {
                log.warn("AI返回格式异常，无message字段：{}", response);
                return "抱歉，AI返回格式异常。";
            }
            log.debug("找到message节点");

            JsonNode content = message.get("content");
            if (content == null) {
                log.warn("AI返回格式异常，无content字段：{}", response);
                return "抱歉，AI返回格式异常。";
            }

            String answer = sanitizeAiOutput(content.asText());
            log.info("成功解析AI答案，长度={}", answer.length());
            return answer;

        } catch (Exception e) {
            log.error("解析AI响应失败，响应内容：{}", response, e);
            return "抱歉，解析AI响应失败。";
        }
    }

    public List<String> refactorKeywords(String question) {
        log.debug("开始关键词重构，原始问题: {}", question);
        
        if (!aiSearchConfig.isEnabled()) {
            log.debug("关键词重构功能未启用，返回原始关键词");
            return List.of(question);
        }

        try {
            log.debug("构建关键词重构Prompt...");
            String prompt = KEYWORD_REFACTOR_PROMPT
                    .replace("{question}", question)
                    .replace("{keywordCount}", String.valueOf(aiSearchConfig.getKeywordCount()));
            
            log.trace("关键词重构Prompt: {}", prompt);

            log.debug("调用AI API进行关键词重构...");
            long startTime = System.currentTimeMillis();
            String aiResponse = callAiApi(prompt);
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("AI返回结果，耗时={}ms", duration);

            List<String> keywords = parseKeywords(aiResponse);
            log.info("关键词重构完成: {} -> {}", question, keywords);
            
            if (keywords.isEmpty()) {
                log.warn("未解析出有效关键词，返回原始问题");
                return List.of(question);
            }

            return keywords;

        } catch (Exception e) {
            log.error("关键词重构过程中发生错误，返回原始关键词作为降级方案", e);
            return List.of(question);
        }
    }

    private List<String> parseKeywords(String aiResponse) {
        log.trace("开始解析关键词...");
        
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            log.warn("AI响应为空");
            return new ArrayList<>();
        }

        String cleanedResponse = sanitizeAiOutput(aiResponse).trim();
        List<String> keywords = new ArrayList<>();

        // 尝试按逗号分割
        String[] parts = cleanedResponse.split("[，,]");
        
        for (String part : parts) {
            String keyword = part.trim();
            if (!keyword.isEmpty()) {
                // 清理关键词（去除编号、引号等）
                keyword = keyword.replaceAll("^\\d+[.、]\\s*", "");
                keyword = keyword.replaceAll("^[\"']|['\"]$", "");
                keyword = keyword.trim();
                if (!keyword.isEmpty()) {
                    keywords.add(keyword);
                }
            }
        }

        // 如果没有解析出关键词，尝试按空格分割
        if (keywords.isEmpty()) {
            keywords = Arrays.stream(cleanedResponse.split("\\s+"))
                    .filter(k -> !k.isEmpty())
                    .collect(Collectors.toList());
        }

        // 限制关键词数量
        int maxCount = aiSearchConfig.getKeywordCount();
        if (keywords.size() > maxCount) {
            keywords = keywords.subList(0, maxCount);
        }

        log.trace("解析完成，得到 {} 个关键词", keywords.size());
        return keywords;
    }

    public String sanitizeAiOutput(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String sanitized = EMOJI_PATTERN.matcher(text).replaceAll("");
        sanitized = UNSUPPORTED_SYMBOL_PATTERN.matcher(sanitized).replaceAll("");
        return sanitized;
    }
}

