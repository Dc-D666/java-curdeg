
package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.configs.ModerationConfig;
import cn.edu.sdu.java.server.payload.response.ModerationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ContentModerationService {

    private static final String SYSTEM_PROMPT = """
身份：学生论坛合规审核员，仅输出指定JSON，无任何额外文字/注释/换行。
任务：审核帖子/评论，优先放过正常发言，边界模糊内容强制转人工。

违规分级：
- 严重违规(reject+serious)：违法犯罪、校园霸凌/人肉/隐私泄露、代考代写作弊、校园贷诈骗、极端歧视/煽动仇恨
- 一般违规(reject+normal)：脏话粗口、刷屏水帖、轻微引战、无关广告、不适内容
- 允许通过(pass+none)：学术讨论、校园吐槽、日常分享、合理诉求

特殊规则：识别谐音/缩写/暗语；匿名涉人内容从严；含图片/链接标注需人工复核；置信度<80%转人工。

输出格式：
{"auditResult":"pass|reject|manual","violationLevel":"serious|normal|none","violationType":"","violationFragments":[],"suggestion":"","confidence":0-100,"remark":""}
""";

    private final RestTemplate restTemplate;
    private final ModerationConfig moderationConfig;
    private final ObjectMapper objectMapper;

    public ContentModerationService(RestTemplate restTemplate, ModerationConfig moderationConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.moderationConfig = moderationConfig;
        this.objectMapper = objectMapper;
    }

    public ModerationResult moderateContent(String title, String content) {
        if (!moderationConfig.isEnabled()) {
            log.info("AI审核功能已禁用，直接通过");
            return ModerationResult.pass();
        }

        String fullContent = String.format("标题：%s\n内容：%s", title, content);
        return moderateText(fullContent);
    }

    private ModerationResult moderateText(String content) {
        log.info("===== 开始AI内容审核 ======");
        try {
            log.info("审核配置 - enabled={}, url={}, model={}", 
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

            ModerationResult result = parseResponse(response);
            log.info("解析结果: auditResult={}, violationLevel={}, confidence={}", 
                result.getAuditResult(),
                result.getViolationLevel(),
                result.getConfidence());

            return result;

        } catch (Exception e) {
            log.error("===== AI审核过程中发生错误 =====", e);
            log.error("错误信息: {}", e.getMessage());
            return ModerationResult.manual();
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
        requestBody.put("temperature", 0);

        return requestBody;
    }

    private ModerationResult parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                log.warn("AI返回格式异常，无choices字段：{}", response);
                return ModerationResult.manual();
            }

            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message == null) {
                log.warn("AI返回格式异常，无message字段：{}", response);
                return ModerationResult.manual();
            }

            String content = message.get("content").asText();
            return parseModerationJson(content);

        } catch (Exception e) {
            log.error("解析AI响应失败，响应内容：{}", response, e);
            return ModerationResult.manual();
        }
    }

    private ModerationResult parseModerationJson(String json) {
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

            ModerationResult result = objectMapper.readValue(cleanedJson, ModerationResult.class);
            
            if (result.getViolationFragments() == null) {
                result.setViolationFragments(new ArrayList<>());
            }
            
            return result;

        } catch (Exception e) {
            log.error("解析审核结果JSON失败，内容：{}", json, e);
            return ModerationResult.manual();
        }
    }
}
