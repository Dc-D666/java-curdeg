package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.configs.ModerationConfig;
import cn.edu.sdu.java.server.models.BbsComment;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.payload.response.ContentSummaryResponse;
import cn.edu.sdu.java.server.repositorys.BbsCommentRepository;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ContentSummaryService {

    private static final String SYSTEM_PROMPT = """
身份：学生论坛内容摘要生成器，仅输出指定JSON，无任何额外文字/注释/换行。

任务：对帖子和评论进行智能摘要，突出重点，语言简洁。

输出格式：
{"postSummary":"帖子摘要","commentHotspots":"评论热点摘要"}
""";

    private static final int MIN_CONTENT_LENGTH = 100;
    private static final int TOP_LIKED_COMMENTS = 5;
    private static final int RECENT_COMMENTS = 3;

    private final RestTemplate restTemplate;
    private final ModerationConfig moderationConfig;
    private final ObjectMapper objectMapper;
    private final BbsPostRepository bbsPostRepository;
    private final BbsCommentRepository bbsCommentRepository;

    public ContentSummaryService(RestTemplate restTemplate, ModerationConfig moderationConfig,
                                 ObjectMapper objectMapper, BbsPostRepository bbsPostRepository,
                                 BbsCommentRepository bbsCommentRepository) {
        this.restTemplate = restTemplate;
        this.moderationConfig = moderationConfig;
        this.objectMapper = objectMapper;
        this.bbsPostRepository = bbsPostRepository;
        this.bbsCommentRepository = bbsCommentRepository;
    }

    public ContentSummaryResponse summarizePost(Long postId) {
        log.info("===== 开始总结帖子内容，postId={} =====", postId);
        try {
            BbsPost post = bbsPostRepository.findById(postId).orElse(null);
            if (post == null) {
                log.warn("帖子不存在，postId={}", postId);
                return ContentSummaryResponse.error("帖子不存在");
            }

            String fullContent = post.getTitle() + "\n" + post.getContent();
            if (fullContent.length() < MIN_CONTENT_LENGTH) {
                log.info("帖子内容过短，无需AI总结，长度={}", fullContent.length());
                return ContentSummaryResponse.info("帖子内容过短，无需AI总结");
            }

            String prompt = buildPostSummaryPrompt(post);
            ContentSummaryResponse result = callAiForSummary(prompt);
            log.info("帖子总结完成，postId={}", postId);
            return result;

        } catch (Exception e) {
            log.error("===== 帖子总结过程中发生错误 =====", e);
            return ContentSummaryResponse.error("总结失败：" + e.getMessage());
        }
    }

    public ContentSummaryResponse summarizePostWithComments(Long postId) {
        log.info("===== 开始总结帖子和评论，postId={} =====", postId);
        try {
            BbsPost post = bbsPostRepository.findById(postId).orElse(null);
            if (post == null) {
                log.warn("帖子不存在，postId={}", postId);
                return ContentSummaryResponse.error("帖子不存在");
            }

            List<BbsComment> comments = selectComments(postId);
            String fullContent = post.getTitle() + "\n" + post.getContent();
            if (fullContent.length() < MIN_CONTENT_LENGTH && comments.isEmpty()) {
                log.info("帖子和评论内容过短，无需AI总结");
                return ContentSummaryResponse.info("帖子和评论内容过短，无需AI总结");
            }

            String prompt = buildPostWithCommentsPrompt(post, comments);
            ContentSummaryResponse result = callAiForSummary(prompt);
            log.info("帖子和评论总结完成，postId={}, 评论数={}", postId, comments.size());
            return result;

        } catch (Exception e) {
            log.error("===== 帖子和评论总结过程中发生错误 =====", e);
            return ContentSummaryResponse.error("总结失败：" + e.getMessage());
        }
    }

    private List<BbsComment> selectComments(Long postId) {
        List<BbsComment> topLiked = bbsCommentRepository.findTopNByPostIdAndStatusOrderByLikeCountDesc(postId, 1, TOP_LIKED_COMMENTS);
        if (topLiked.size() >= TOP_LIKED_COMMENTS) {
            log.info("选择点赞数前{}条评论", TOP_LIKED_COMMENTS);
            return topLiked;
        }
        log.info("点赞数评论不足{}条，选择最新{}条评论", TOP_LIKED_COMMENTS, RECENT_COMMENTS);
        List<BbsComment> allComments = bbsCommentRepository.findByPostId(postId);
        return allComments.stream()
                .filter(c -> c.getStatus() == 1)
                .sorted((c1, c2) -> c2.getCreateTime().compareTo(c1.getCreateTime()))
                .limit(RECENT_COMMENTS)
                .toList();
    }

    private String buildPostSummaryPrompt(BbsPost post) {
        return String.format("请对以下帖子进行摘要：\n标题：%s\n内容：%s",
                post.getTitle(), post.getContent());
    }

    private String buildPostWithCommentsPrompt(BbsPost post, List<BbsComment> comments) {
        StringBuilder sb = new StringBuilder();
        sb.append("请对以下帖子和评论进行摘要：\n");
        sb.append("标题：").append(post.getTitle()).append("\n");
        sb.append("内容：").append(post.getContent()).append("\n");
        sb.append("评论：\n");
        for (int i = 0; i < comments.size(); i++) {
            BbsComment comment = comments.get(i);
            sb.append(String.format("[%d] 点赞数:%d - %s\n",
                    i + 1, comment.getLikeCount(), comment.getContent()));
        }
        return sb.toString();
    }

    private ContentSummaryResponse callAiForSummary(String content) {
        log.info("===== 开始调用AI生成摘要 =====");
        try {
            log.info("配置 - enabled={}, url={}, model={}",
                    moderationConfig.isEnabled(),
                    moderationConfig.getApi().getUrl(),
                    moderationConfig.getApi().getModel());

            Map<String, Object> requestBody = buildRequestBody(content);

            log.info("请求体构建完成，model={}, messages数量={}",
                    requestBody.get("model"),
                    ((List) requestBody.get("messages")).size());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(moderationConfig.getApi().getKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("发送请求到: {}", moderationConfig.getApi().getUrl());

            long startTime = System.currentTimeMillis();
            String response = restTemplate.postForObject(moderationConfig.getApi().getUrl(), request, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("收到响应，耗时={}ms, 响应长度={}", duration, response != null ? response.length() : 0);
            log.info("响应内容: {}", response);

            ContentSummaryResponse result = parseResponse(response);
            log.info("解析结果: success={}", result.isSuccess());

            return result;

        } catch (Exception e) {
            log.error("===== AI摘要生成过程中发生错误 =====", e);
            log.error("错误信息: {}", e.getMessage());
            return ContentSummaryResponse.error("AI调用失败：" + e.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", content);

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", moderationConfig.getApi().getModel());
        requestBody.put("messages", List.of(systemMessage, message));
        requestBody.put("temperature", 0.7);

        return requestBody;
    }

    private ContentSummaryResponse parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                log.warn("AI返回格式异常，无choices字段：{}", response);
                return ContentSummaryResponse.error("AI返回格式异常");
            }

            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message == null) {
                log.warn("AI返回格式异常，无message字段：{}", response);
                return ContentSummaryResponse.error("AI返回格式异常");
            }

            String content = message.get("content").asText();
            return parseSummaryJson(content);

        } catch (Exception e) {
            log.error("解析AI响应失败，响应内容：{}", response, e);
            return ContentSummaryResponse.error("解析AI响应失败");
        }
    }

    private ContentSummaryResponse parseSummaryJson(String json) {
        try {
            String cleanedJson = json.trim();
            if (!cleanedJson.startsWith("{")) {
                int jsonStart = cleanedJson.indexOf("{");
                if (jsonStart >= 0) {
                    cleanedJson = cleanedJson.substring(jsonStart);
                }
            }
            if (!cleanedJson.endsWith("}")) {
                int jsonEnd = cleanedJson.lastIndexOf("}");
                if (jsonEnd >= 0) {
                    cleanedJson = cleanedJson.substring(0, jsonEnd + 1);
                }
            }

            JsonNode jsonNode = objectMapper.readTree(cleanedJson);
            String postSummary = jsonNode.has("postSummary") ? jsonNode.get("postSummary").asText() : "";
            String commentHotspots = jsonNode.has("commentHotspots") ? jsonNode.get("commentHotspots").asText() : 
                                     jsonNode.has("commentSummary") ? jsonNode.get("commentSummary").asText() : "";

            return ContentSummaryResponse.success(postSummary, commentHotspots);

        } catch (Exception e) {
            log.error("解析摘要结果JSON失败，内容：{}", json, e);
            return ContentSummaryResponse.error("解析摘要结果失败");
        }
    }
}
