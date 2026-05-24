package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsFollowService;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/follow")
public class BbsFollowController {

    private final BbsFollowService bbsFollowService;

    public BbsFollowController(BbsFollowService bbsFollowService) {
        this.bbsFollowService = bbsFollowService;
    }

    @PostMapping("/toggle/{followingId}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse toggleFollow(@PathVariable Integer followingId) {
        return bbsFollowService.toggleFollow(followingId);
    }

    @GetMapping("/status/{followingId}")
    public DataResponse checkFollowStatus(@PathVariable Integer followingId) {
        return bbsFollowService.checkFollowStatus(followingId);
    }

    @GetMapping("/following/{userId}")
    public DataResponse getFollowingList(@PathVariable Integer userId) {
        return bbsFollowService.getFollowingList(userId);
    }

    @GetMapping("/follower/{userId}")
    public DataResponse getFollowerList(@PathVariable Integer userId) {
        return bbsFollowService.getFollowerList(userId);
    }

    @GetMapping("/my-following")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyFollowingList() {
        Integer currentUserId = CommonMethod.getPersonId();
        return bbsFollowService.getFollowingList(currentUserId);
    }

    @GetMapping("/my-follower")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyFollowerList() {
        Integer currentUserId = CommonMethod.getPersonId();
        return bbsFollowService.getFollowerList(currentUserId);
    }

    @GetMapping("/my-following/page")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyFollowingListPage(@RequestParam Map<String, String> params) {
        Integer currentUserId = CommonMethod.getPersonId();
        int pageNum = params.containsKey("pageNum") ? Integer.parseInt(params.get("pageNum")) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize")) : 10;
        return bbsFollowService.getFollowingListPage(currentUserId, pageNum, pageSize);
    }

    @GetMapping("/my-follower/page")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyFollowerListPage(@RequestParam Map<String, String> params) {
        Integer currentUserId = CommonMethod.getPersonId();
        int pageNum = params.containsKey("pageNum") ? Integer.parseInt(params.get("pageNum")) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize")) : 10;
        return bbsFollowService.getFollowerListPage(currentUserId, pageNum, pageSize);
    }
}
