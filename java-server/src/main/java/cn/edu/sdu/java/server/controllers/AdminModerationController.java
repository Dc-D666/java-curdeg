
package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.AdminModerationService;
import cn.edu.sdu.java.server.services.BbsPostService;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import cn.edu.sdu.java.server.models.BbsPost;

@Slf4j
@RestController
@RequestMapping("/api/admin/moderation")
public class AdminModerationController {

    @Autowired
    private AdminModerationService adminModerationService;

    @GetMapping("/pending")
    public DataResponse getPendingPosts(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("===== AdminModerationController.getPendingPosts =====");
        log.info("请求参数 - pageNum={}, pageSize={}", pageNum, pageSize);

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 20;
        }

        log.info("最终参数 - pageNum={}, pageSize={}", pageNum, pageSize);

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BbsPost> posts = adminModerationService.getPendingPosts(pageable);

        log.info("返回结果 - 数量={}, 总数={}", 
            posts != null ? posts.getContent().size() : 0,
            posts != null ? posts.getTotalElements() : 0);

        return CommonMethod.getReturnData(posts);
    }

    @GetMapping("/posts")
    public DataResponse getAllPosts(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("===== AdminModerationController.getAllPosts =====");
        log.info("请求参数 - pageNum={}, pageSize={}", pageNum, pageSize);

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 20;
        }

        log.info("最终参数 - pageNum={}, pageSize={}", pageNum, pageSize);

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BbsPost> posts = adminModerationService.getAllPosts(pageable);

        log.info("返回结果 - 数量={}, 总数={}", 
            posts != null ? posts.getContent().size() : 0,
            posts != null ? posts.getTotalElements() : 0);

        return CommonMethod.getReturnData(posts);
    }

    @PostMapping("/{postId}/review")
    public DataResponse reviewPost(@PathVariable Long postId,
                                    @RequestBody DataRequest dataRequest) {
        log.info("===== AdminModerationController.reviewPost =====");
        log.info("请求参数 - postId={}", postId);
        log.info("请求体 - {}", dataRequest);

        String decision = dataRequest.getString("decision"); // pass, reject
        String violationLevel = dataRequest.getString("violationLevel");
        String violationType = dataRequest.getString("violationType");
        String remark = dataRequest.getString("remark");

        log.info("审核信息 - decision={}, violationLevel={}, violationType={}, remark={}",
            decision, violationLevel, violationType, remark);

        if (postId == null) {
            log.warn("帖子ID为空");
            return CommonMethod.getReturnMessageError("帖子ID不能为空");
        }
        if (decision == null || (!"pass".equals(decision) && !"reject".equals(decision))) {
            log.warn("无效的审核决策: {}", decision);
            return CommonMethod.getReturnMessageError("审核决策必须是 pass 或 reject");
        }

        Integer currentUserId = CommonMethod.getPersonId();
        log.info("当前管理员ID: {}", currentUserId);
        
        adminModerationService.manualModerate(postId, decision, violationLevel, violationType, remark, currentUserId);

        log.info("审核完成");
        return CommonMethod.getReturnMessageOK("审核完成");
    }
}

