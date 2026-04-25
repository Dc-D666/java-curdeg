
package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.AiImageRequest;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.AiImageResponse;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.AiImageService;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/ai-image")
public class AiImageController {

    private final AiImageService aiImageService;

    public AiImageController(AiImageService aiImageService) {
        this.aiImageService = aiImageService;
    }

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public DataResponse generateImage(@Valid @RequestBody DataRequest dataRequest) {
        log.info("===== 收到AI图片生成请求 =====");
        String prompt = dataRequest.getString("prompt");
        String size = dataRequest.getString("size");
        
        log.info("prompt={}", prompt);
        log.info("size={}", size);
        
        AiImageRequest request = AiImageRequest.builder()
            .prompt(prompt)
            .size(size)
            .build();
        
        try {
            AiImageResponse result = aiImageService.generateImage(request);
            if (result.isSuccess()) {
                return CommonMethod.getReturnData(result, "AI图片生成完成");
            } else {
                return CommonMethod.getReturnMessageError(result.getMessage());
            }
        } catch (Exception e) {
            log.error("AI图片生成请求处理失败", e);
            return CommonMethod.getReturnMessageError("AI图片生成失败：" + e.getMessage());
        }
    }
}
