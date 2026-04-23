package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsFileService;
import cn.edu.sdu.java.server.util.CommonMethod;
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
    public DataResponse uploadImage(@RequestParam("file") MultipartFile file) {
        String url = bbsFileService.uploadImage(file);
        if (url != null) {
            return CommonMethod.getReturnData(url);
        } else {
            return CommonMethod.getReturnMessageError("图片上传失败，请检查文件格式和大小");
        }
    }
}
