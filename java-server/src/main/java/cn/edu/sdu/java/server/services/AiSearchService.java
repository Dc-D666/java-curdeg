
package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.configs.ModerationConfig;
import cn.edu.sdu.java.server.models.BbsComment;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsCommentRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class AiSearchService {

    private static final String SYSTEM_PROMPT = """
你是校园论坛智能助手。根据以下帖子回答用户问题。

【相关帖子】
{postsContent}

【用户问题】
{question}

【回答要求】
1. 先总结帖子中的相关讨论内容
2. 然后给出针对性的建议
3. 如果没有相关帖子，明确告知用户，然后用你的知识给出一般性建议
4. 回答简洁友好，符合校园论坛氛围
5. 不要编造信息
6. 使用Markdown格式：**加粗**, *斜体*, ~~删除线~~
7. 重要的关键词或重点内容用[红色]标红文本[/红色]标红
8. 段落之间用换行分隔
"""; 

    private final BbsPostService bbsPostService;
    private final BbsCommentRepository bbsCommentRepository;
    private final RestTemplate restTemplate;
    private final ModerationConfig moderationConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AiSearchService(BbsPostService bbsPostService, BbsCommentRepository bbsCommentRepository,
                          RestTemplate restTemplate, ModerationConfig moderationConfig, ObjectMapper objectMapper) {
        this.bbsPostService = bbsPostService;
        this.bbsCommentRepository = bbsCommentRepository;
        this.restTemplate = restTemplate;
        this.moderationConfig = moderationConfig;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
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
     * 搜索帖子和评论（供流式接口使用）
     */
    public List<BbsPost> searchPostsOnly(String keyword) {
        log.info("开始搜索帖子... (搜索模式: fulltext全文搜索)");
        DataResponse searchResponse = bbsPostService.searchPosts(keyword, "fulltext", 1, 5);
        Page<BbsPost> postPage = (Page<BbsPost>) searchResponse.getData();
        List<BbsPost> posts = postPage != null ? postPage.getContent() : new ArrayList<>();
        log.info("搜索完成，找到 {} 个相关帖子", posts.size());
        return posts;
    }

    /**
     * 构建带评论的帖子列表
     */
    public List<PostWithComments> buildPostWithComments(List<BbsPost> posts) {
        log.info("开始获取评论...");
        List<PostWithComments> postWithCommentsList = new ArrayList<>();
        int totalComments = 0;

        for (BbsPost post : posts) {
            List<BbsComment> comments = bbsCommentRepository.findTop5ByPostIdAndStatusOrderByLikeCountDesc(post.getId(), 1);
            log.info("  帖子 {} (id={}) 找到 {} 条评论", post.getTitle(), post.getId(), comments.size());
            postWithCommentsList.add(new PostWithComments(post, comments));
            totalComments += comments.size();
        }
        log.info("评论获取完成，共 {} 条", totalComments);
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
        log.debug("--- 完整Prompt开始 ---\n{}\n--- 完整Prompt结束 ---", prompt);
        return prompt;
    }

    /**
     * 流式调用AI API
     */
    public void callAiApiStream(String prompt, Consumer<String> onContent, Runnable onComplete, Consumer<Exception> onError) {
        log.info("===== 开始流式调用AI API =====");
        new Thread(() -> {
            try {
                log.info("1/3: 构建请求体...");
                Map<String, Object> requestBody = buildRequestBodyStream(prompt);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(moderationConfig.getApi().getUrl()))
                        .headers("Content-Type", "application/json")
                        .headers("Authorization", "Bearer " + moderationConfig.getApi().getKey())
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                        .build();

                log.info("2/3: 发送流式HTTP请求...");
                long startTime = System.currentTimeMillis();
                HttpResponse<java.io.InputStream> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofInputStream());
                
                log.info("3/3: 开始接收流式响应... (耗时: {}ms)", System.currentTimeMillis() - startTime);
                
                java.io.InputStream inputStream = response.body();
                // ⚠️ 关键：不使用 BufferedReader 缓冲！直接无缓冲读取！
                java.io.InputStreamReader isr = new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);
                
                boolean completed = false;
                int totalChunks = 0;
                StringBuilder lineBuffer = new StringBuilder();
                int c;
                while ((c = isr.read()) != -1) {
                    if (c == '\n') {
                        // 读完一行
                        String line = lineBuffer.toString();
                        lineBuffer.setLength(0); // 清空 buffer
                        
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data.trim())) {
                                log.info("[{}ms] 接收到流式结束标记，共收到{}个chunk", System.currentTimeMillis() - startTime, totalChunks);
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
                                        String content = delta.get("content").asText();
                                        totalChunks++;
                                        long timeSinceStart = System.currentTimeMillis() - startTime;
                                        log.info("[{}ms] AI返回chunk #{}: '{}'", timeSinceStart, totalChunks, content);
                                        onContent.accept(content);
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("解析chunk失败: {}", e.getMessage());
                            }
                        }
                    } else if (c != '\r') {
                        // 忽略 \r，只追加其他字符
                        lineBuffer.append((char) c);
                    }
                }
                
                if (!completed) {
                    log.info("[{}ms] 未收到结束标记，手动完成", System.currentTimeMillis() - startTime);
                    onComplete.run();
                }
                
            } catch (Exception e) {
                log.error("===== 流式调用AI API过程中发生错误 =====", e);
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
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", true);

        return requestBody;
    }

    public Map<String, Object> aiSearch(String keyword) {
        log.info("===== 开始AI智能搜索 =====");
        log.info("搜索关键词: {}", keyword);

        try {
            log.info("1/5: 开始搜索帖子... (搜索模式: fulltext全文搜索)");
            DataResponse searchResponse = bbsPostService.searchPosts(keyword, "fulltext", 1, 5);
            Page<BbsPost> postPage = (Page<BbsPost>) searchResponse.getData();
            List<BbsPost> posts = postPage != null ? postPage.getContent() : new ArrayList<>();
            log.info("2/5: 搜索完成，找到 {} 个相关帖子", posts.size());
            
            // 输出帖子详情
            for (int i = 0; i < posts.size(); i++) {
                BbsPost post = posts.get(i);
                log.info("  帖子 {}: id={}, title={}, author={}", 
                         i+1, post.getId(), post.getTitle(), post.getAuthorNickname());
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

            log.info("===== AI智能搜索完成 =====");
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
        requestBody.put("temperature", 0.7);

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

            String answer = content.asText();
            log.info("成功解析AI答案，长度={}", answer.length());
            return answer;

        } catch (Exception e) {
            log.error("解析AI响应失败，响应内容：{}", response, e);
            return "抱歉，解析AI响应失败。";
        }
    }
}

