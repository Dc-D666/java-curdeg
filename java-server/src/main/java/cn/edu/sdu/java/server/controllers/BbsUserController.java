package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/user")
public class BbsUserController {

    private final BbsUserService bbsUserService;

    public BbsUserController(BbsUserService bbsUserService) {
        this.bbsUserService = bbsUserService;
    }

    @PostMapping("/register")
    public DataResponse register(@Valid @RequestBody DataRequest dataRequest) {
        return bbsUserService.register(dataRequest);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getCurrentUser() {
        return bbsUserService.getCurrentUser();
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public DataResponse updateCurrentUser(@Valid @RequestBody DataRequest dataRequest) {
        return bbsUserService.updateCurrentUser(dataRequest);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getUserList(@Valid DataRequest dataRequest) {
        return bbsUserService.getUserList(dataRequest);
    }

    @GetMapping("/me/statistics")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getUserStatistics() {
        return bbsUserService.getUserStatistics();
    }

    @GetMapping("/me/statistics/detail")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getUserStatisticsDetail() {
        return bbsUserService.getUserStatisticsDetail();
    }

    @GetMapping("/me/posts")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyPosts(@Valid DataRequest dataRequest) {
        return bbsUserService.getMyPosts(dataRequest);
    }

    @GetMapping("/me/favorites")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyFavorites(@Valid DataRequest dataRequest) {
        return bbsUserService.getMyFavorites(dataRequest);
    }

    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public DataResponse changePassword(@Valid @RequestBody DataRequest dataRequest) {
        return bbsUserService.changePassword(dataRequest);
    }

    @PostMapping("/me/password/code")
    @PreAuthorize("isAuthenticated()")
    public DataResponse sendChangePasswordCode() {
        return bbsUserService.sendChangePasswordCode();
    }

    @GetMapping("/{userId}")
    public DataResponse getUserProfile(@PathVariable Integer userId) {
        return bbsUserService.getUserProfile(userId);
    }
}
