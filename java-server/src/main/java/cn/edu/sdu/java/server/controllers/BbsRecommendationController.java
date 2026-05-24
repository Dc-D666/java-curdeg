package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsRecommendationService;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bbs/recommendation")
@RequiredArgsConstructor
public class BbsRecommendationController {

    private final BbsRecommendationService bbsRecommendationService;

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getRecommendations() {
        Map<String, Object> result = bbsRecommendationService.getRecommendations();
        return CommonMethod.getReturnData(result);
    }

    @PostMapping("/browse-history")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getBrowseHistoryList(@Valid @RequestBody DataRequest dataRequest) {
        Integer page = dataRequest.getInteger("page");
        Integer size = dataRequest.getInteger("size");
        if (page == null) page = 0;
        if (size == null) size = 10;
        Map<String, Object> result = bbsRecommendationService.getBrowseHistoryList(page, size);
        return CommonMethod.getReturnData(result);
    }

    @PostMapping("/browse")
    @PreAuthorize("isAuthenticated()")
    public DataResponse recordBrowse(@Valid @RequestBody DataRequest dataRequest) {
        Long postId = dataRequest.getLong("postId");
        if (postId == null) {
            return CommonMethod.getReturnMessageError("帖子ID不能为空");
        }
        bbsRecommendationService.recordBrowseHistory(postId);
        return CommonMethod.getReturnMessageOK("记录成功");
    }

    @PostMapping("/clear-history")
    @PreAuthorize("isAuthenticated()")
    public DataResponse clearBrowseHistory() {
        bbsRecommendationService.clearBrowseHistory();
        return CommonMethod.getReturnMessageOK("浏览历史已清除");
    }
}
