package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.configs.ModerationConfig;
import cn.edu.sdu.java.server.payload.request.AiWriteRequest;
import cn.edu.sdu.java.server.payload.response.AiWriteResponse;
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
public class AiWriteService {

    private static final String SYSTEM_PROMPT = """
身份：学生论坛AI写作助手，仅输出指定JSON，无任何额外文字/注释/换行。

任务：根据用户提供的信息帮助用户写作或优化帖子：
1. 如果用户提供了标题但没有内容，请根据标题生成合适的校园论坛帖子内容
2. 如果用户提供了内容，请根据用户指令进行优化或续写
3. 输出必须为严格的JSON格式

输出格式：
{"title":"优化后的标题","content":"优化后的正文内容","instructionSuggestion":"对用户指令的优化建议（可选）"}
""";

    private final RestTemplate restTemplate;
    private final ModerationConfig moderationConfig;
    private final ObjectMapper objectMapper;

    public AiWriteService(RestTemplate restTemplate, ModerationConfig moderationConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.moderationConfig = moderationConfig;
        this.objectMapper = objectMapper;
    }

    public AiWriteResponse aiWrite(AiWriteRequest request) {
        log.info("===== 开始AI写作 =====");
        log.info("请求 - title={}, content={}, instruction={}, operation={}", 
                request.getTitle(), request.getContent(), request.getInstruction(), request.getOperation());
        
        try {
            String prompt = buildPrompt(request);
            log.info("构建好的prompt: {}", prompt);
            AiWriteResponse result = callAiForWriting(prompt);
            log.info("AI写作完成，success={}", result.isSuccess());
            return result;

        } catch (Exception e) {
            log.error("===== AI写作过程中发生错误 =====", e);
            return AiWriteResponse.error("AI调用失败：" + e.getMessage());
        }
    }

    private String buildPrompt(AiWriteRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请作为校园论坛AI写作助手帮助用户：\n");
        
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            sb.append("标题：").append(request.getTitle()).append("\n");
        }
        
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            sb.append("现有内容：").append(request.getContent()).append("\n");
        }
        
        sb.append("用户指令：").append(request.getInstruction()).append("\n");
        
        if (request.getOperation() != null) {
            sb.append("操作类型：").append(request.getOperation()).append("\n");
        }
        
        return sb.toString();
    }

    private AiWriteResponse callAiForWriting(String content) {
        log.info("===== 开始调用AI进行写作 =====");
        try {
            log.info("配置 - enabled={}, url={}, model={}",
                    moderationConfig.isEnabled(),
                    moderationConfig.getApi().getUrl(),
                    moderationConfig.getApi().getModel());

            Map<String, Object> requestBody = buildRequestBody(content);

            log.info("请求体构建完成，model={}, messages数量={}",
                    requestBody.get("model"),
                    ((List) requestBody.get("messages")).size());
            
            // 打印完整的请求体
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            log.info("===== 完整请求体 =====");
            log.info(requestBodyJson);
            log.info("===== 请求体结束 =====");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(moderationConfig.getApi().getKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("发送请求到: {}", moderationConfig.getApi().getUrl());

            long startTime = System.currentTimeMillis();
            String response = restTemplate.postForObject(moderationConfig.getApi().getUrl(), request, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("收到响应，耗时={}ms, 响应长度={}", duration, response != null ? response.length() : 0);
            log.info("===== 完整响应内容 =====");
            log.info(response);
            log.info("===== 响应结束 =====");

            AiWriteResponse result = parseResponse(response);
            log.info("解析结果: success={}", result.isSuccess());

            return result;

        } catch (Exception e) {
            log.error("===== AI写作生成过程中发生错误 =====", e);
            log.error("错误信息: {}", e.getMessage());
            return AiWriteResponse.error("AI调用失败：" + e.getMessage());
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

    private AiWriteResponse parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                log.warn("AI返回格式异常，无choices字段：{}", response);
                return AiWriteResponse.error("AI返回格式异常");
            }

            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message == null) {
                log.warn("AI返回格式异常，无message字段：{}", response);
                return AiWriteResponse.error("AI返回格式异常");
            }

            String content = message.get("content").asText();
            return parseAiWriteJson(content);

        } catch (Exception e) {
            log.error("解析AI响应失败，响应内容：{}", response, e);
            return AiWriteResponse.error("解析AI响应失败");
        }
    }

    private AiWriteResponse parseAiWriteJson(String json) {
        try {
            log.info("开始解析AI返回内容：{}", json);
            
            String cleanedJson = json.trim();
            log.info("清理前长度={}, 清理后长度={}", json.length(), cleanedJson.length());
            
            if (!cleanedJson.startsWith("{")) {
                log.info("内容不以 { 开头，尝试提取 JSON 部分");
                int jsonStart = cleanedJson.indexOf("{");
                if (jsonStart >= 0) {
                    cleanedJson = cleanedJson.substring(jsonStart);
                    log.info("提取 JSON 从索引 {} 开始：{}", jsonStart, cleanedJson);
                } else {
                    log.warn("内容中没有找到 {，可能不是 JSON 格式：{}", json);
                    // 如果 AI 返回的是纯文本，我们尝试用它作为内容
                    if (!json.isEmpty()) {
                        log.info("尝试将纯文本内容作为返回结果");
                        return AiWriteResponse.success("", json, "");
                    }
                    return AiWriteResponse.error("AI返回格式异常");
                }
            }
            if (!cleanedJson.endsWith("}")) {
                log.info("内容不以 } 结尾，尝试提取 JSON 部分");
                int jsonEnd = cleanedJson.lastIndexOf("}");
                if (jsonEnd >= 0) {
                    cleanedJson = cleanedJson.substring(0, jsonEnd + 1);
                    log.info("提取 JSON 到索引 {}：{}", jsonEnd, cleanedJson);
                }
            }

            log.info("最终解析的 JSON：{}", cleanedJson);
            JsonNode jsonNode = objectMapper.readTree(cleanedJson);
            String title = jsonNode.has("title") ? jsonNode.get("title").asText() : "";
            String content = jsonNode.has("content") ? jsonNode.get("content").asText() : "";
            String instructionSuggestion = jsonNode.has("instructionSuggestion") ? 
                    jsonNode.get("instructionSuggestion").asText() : "";

            log.info("解析成功 - title={}, content长度={}", title, content.length());
            return AiWriteResponse.success(title, content, instructionSuggestion);

        } catch (Exception e) {
            log.error("解析AI写作结果JSON失败，原始内容：{}", json, e);
            // 如果解析失败，尝试将原始内容直接作为 content 返回
            if (json != null && !json.trim().isEmpty()) {
                log.info("解析失败，尝试将原始内容作为 content 返回");
                return AiWriteResponse.success("", json, "");
            }
            return AiWriteResponse.error("解析AI写作结果失败");
        }
    }
}
