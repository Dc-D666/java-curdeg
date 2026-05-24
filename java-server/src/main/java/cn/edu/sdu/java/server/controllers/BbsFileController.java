package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsFileService;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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
}
