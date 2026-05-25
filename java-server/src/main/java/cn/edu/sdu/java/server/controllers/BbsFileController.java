package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsFileService;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/file")
public class BbsFileController {

    private final BbsFileService bbsFileService;

    public BbsFileController(BbsFileService bbsFileService) {
        this.bbsFileService = bbsFileService;
    }

    @PostMapping("/upload-image")
    @PreAuthorize("isAuthenticated()")
    public DataResponse uploadImage(@RequestParam("file") MultipartFile file) {
        String url = bbsFileService.uploadImage(file);
        if (url != null) {
            return CommonMethod.getReturnData(url);
        } else {
            return CommonMethod.getReturnMessageError("图片上传失败，请检查文件格式和大小");
        }
    }

    @PostMapping("/upload-attachment")
    @PreAuthorize("isAuthenticated()")
    public DataResponse uploadAttachment(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> attachmentInfo = bbsFileService.uploadAttachment(file);
            return CommonMethod.getReturnData(attachmentInfo);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonMethod.getReturnMessageError(e.getMessage());
        }
    }

    @PostMapping("/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> downloadAttachment(@RequestBody DataRequest dataRequest) {
        try {
            String url = dataRequest.getString("url");
            String fileName = dataRequest.getString("fileName");

            Path attachmentPath = bbsFileService.resolveAttachmentPath(url);
            if (attachmentPath == null || !Files.exists(attachmentPath) || !Files.isRegularFile(attachmentPath)) {
                return ResponseEntity.status(404)
                        .body(CommonMethod.getReturnMessageError("附件文件不存在，可能已被删除或尚未同步到本机"));
            }

            String downloadName = (fileName == null || fileName.isBlank())
                    ? attachmentPath.getFileName().toString()
                    : fileName;
            String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");
            String contentType = Files.probeContentType(attachmentPath);
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            Resource resource = new FileSystemResource(attachmentPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(Files.size(attachmentPath))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedName)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(CommonMethod.getReturnMessageError("附件下载失败：" + e.getMessage()));
        }
    }
}
