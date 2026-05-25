package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.services.BbsFileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@RestController
public class UploadResourceController {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "webp"
    );

    private final BbsFileService bbsFileService;

    public UploadResourceController(BbsFileService bbsFileService) {
        this.bbsFileService = bbsFileService;
    }

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> getUploadResource(HttpServletRequest request) throws IOException {
        String requestUri = request.getRequestURI();
        Path filePath = bbsFileService.resolveAttachmentPath(requestUri);
        if (filePath != null && Files.exists(filePath) && Files.isRegularFile(filePath)) {
            String contentType = Files.probeContentType(filePath);
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new FileSystemResource(filePath));
        }

        if (isImageRequest(requestUri)) {
            ClassPathResource placeholder = new ClassPathResource("public/missing-image.png");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(placeholder);
        }

        return ResponseEntity.notFound().build();
    }

    private boolean isImageRequest(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return false;
        }
        int lastDotIndex = requestUri.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == requestUri.length() - 1) {
            return false;
        }
        String ext = requestUri.substring(lastDotIndex + 1).toLowerCase();
        return IMAGE_EXTENSIONS.contains(ext);
    }
}
