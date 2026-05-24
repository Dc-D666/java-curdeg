
package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.configs.AiImageConfig;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.AiImageRequest;
import cn.edu.sdu.java.server.payload.response.AiImageResponse;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AiImageService {

    private final RestTemplate restTemplate;
    private final AiImageConfig aiImageConfig;
    private final ObjectMapper objectMapper;
    private final BbsFileService bbsFileService;
    private final UserRepository userRepository;
    private final LevelPrivilegeService levelPrivilegeService;

    public AiImageService(RestTemplate restTemplate, AiImageConfig aiImageConfig, 
                         ObjectMapper objectMapper, BbsFileService bbsFileService,
                         UserRepository userRepository, LevelPrivilegeService levelPrivilegeService) {
        this.restTemplate = restTemplate;
        this.aiImageConfig = aiImageConfig;
        this.objectMapper = objectMapper;
        this.bbsFileService = bbsFileService;
        this.userRepository = userRepository;
        this.levelPrivilegeService = levelPrivilegeService;
    }

    public AiImageResponse generateImage(AiImageRequest request) {
        return generateImage(request, null);
    }

    public AiImageResponse generateImage(AiImageRequest request, Integer userId) {
        log.info("===== 开始AI图片生成 =====");
        log.info("请求 - prompt={}, size={}", request.getPrompt(), request.getSize());
        
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                int imageLimit = levelPrivilegeService.getAiImageLimit(user.getLevel());
                if (imageLimit <= 0) {
                    return AiImageResponse.error("您当前等级不具备AI图片生成权限");
                }
                if (!levelPrivilegeService.checkAiUsageLimit(userId, "image", imageLimit)) {
                    return AiImageResponse.error("您今日AI配图次数已达上限(" + imageLimit + "次)，请明日再来");
                }
            }
        }
        
        try {
            String imageUrlFromAi = callAiForImage(request);
            if (imageUrlFromAi == null) {
                return AiImageResponse.error("AI图片生成失败");
            }
            
            log.info("AI返回图片URL，开始下载保存到本地: {}", imageUrlFromAi);
            String localImageUrl = bbsFileService.downloadAndSaveImage(imageUrlFromAi);
            
            if (localImageUrl == null) {
                return AiImageResponse.error("图片保存失败");
            }
            
            log.info("AI图片生成完成，本地URL: {}", localImageUrl);
            return AiImageResponse.success(localImageUrl);
            
        } catch (Exception e) {
            log.error("===== AI图片生成过程中发生错误 =====", e);
            return AiImageResponse.error("AI调用失败：" + e.getMessage());
        }
    }

    private String callAiForImage(AiImageRequest request) {
        log.info("===== 开始调用AI生成图片 =====");
        try {
            log.info("配置 - enabled={}, url={}, model={}",
                    aiImageConfig.isEnabled(),
                    aiImageConfig.getApi().getUrl(),
                    aiImageConfig.getApi().getModel());

            Map<String, Object> requestBody = buildRequestBody(request);

            log.info("请求体构建完成");
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            log.info("===== 完整请求体 =====");
            log.info(requestBodyJson);
            log.info("===== 请求体结束 =====");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiImageConfig.getApi().getKey());

            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

            log.info("发送请求到: {}", aiImageConfig.getApi().getUrl());

            long startTime = System.currentTimeMillis();
            String response = restTemplate.postForObject(aiImageConfig.getApi().getUrl(), httpRequest, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("收到响应，耗时={}ms, 响应长度={}", duration, response != null ? response.length() : 0);
            log.info("===== 完整响应内容 =====");
            log.info(response);
            log.info("===== 响应结束 =====");

            String imageUrl = parseResponse(response);
            log.info("解析图片URL结果: {}", imageUrl);

            return imageUrl;

        } catch (Exception e) {
            log.error("===== AI图片生成过程中发生错误 =====", e);
            log.error("错误信息: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> buildRequestBody(AiImageRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiImageConfig.getApi().getModel());
        requestBody.put("prompt", request.getPrompt());
        
        String size = request.getSize() != null && !request.getSize().isEmpty() ? request.getSize() : "1024x1024";
        requestBody.put("size", size);
        
        requestBody.put("n", 1);
        
        return requestBody;
    }

    private String parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.get("data");
            
            if (data == null || !data.isArray() || data.isEmpty()) {
                log.warn("AI返回格式异常，无data字段：{}", response);
                return null;
            }
            
            JsonNode firstItem = data.get(0);
            JsonNode url = firstItem.get("url");
            
            if (url == null) {
                log.warn("AI返回格式异常，无url字段：{}", response);
                return null;
            }
            
            return url.asText();
            
        } catch (Exception e) {
            log.error("解析AI图片响应失败，响应内容：{}", response, e);
            return null;
        }
    }
}
