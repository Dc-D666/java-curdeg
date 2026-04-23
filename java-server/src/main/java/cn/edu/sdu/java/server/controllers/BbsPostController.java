package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsPostService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/post")
public class BbsPostController {

    private final BbsPostService bbsPostService;

    public BbsPostController(BbsPostService bbsPostService) {
        this.bbsPostService = bbsPostService;
    }

    @GetMapping("/list")
    public DataResponse getPostList(@RequestParam Map<String, String> params) {
        DataRequest dataRequest = new DataRequest();
        params.forEach((key, value) -> {
            try {
                if ("pageNum".equals(key) || "pageSize".equals(key)) {
                    dataRequest.add(key, Integer.parseInt(value));
                } else if ("boardId".equals(key)) {
                    dataRequest.add(key, Long.parseLong(value));
                } else {
                    dataRequest.add(key, value);
                }
            } catch (Exception e) {
                dataRequest.add(key, value);
            }
        });
        return bbsPostService.getPostList(dataRequest);
    }

    @GetMapping("/{id}")
    public DataResponse getPostDetail(@PathVariable Long id) {
        return bbsPostService.getPostDetail(id);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public DataResponse createPost(@Valid @RequestBody DataRequest dataRequest) {
        return bbsPostService.createPost(dataRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse updatePost(@PathVariable Long id, @Valid @RequestBody DataRequest dataRequest) {
        return bbsPostService.updatePost(id, dataRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse deletePost(@PathVariable Long id) {
        return bbsPostService.deletePost(id);
    }

    @PostMapping("/{id}/top")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse toggleTop(@PathVariable Long id) {
        return bbsPostService.toggleTop(id);
    }

    @PostMapping("/{id}/feature")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse toggleFeature(@PathVariable Long id) {
        return bbsPostService.toggleFeature(id);
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public DataResponse toggleLike(@PathVariable Long id) {
        return bbsPostService.toggleLike(id);
    }

    @GetMapping("/{id}/like/status")
    public DataResponse getLikeStatus(@PathVariable Long id) {
        return bbsPostService.getLikeStatus(id);
    }

    @PostMapping("/{id}/favorite")
    @PreAuthorize("isAuthenticated()")
    public DataResponse toggleFavorite(@PathVariable Long id) {
        return bbsPostService.toggleFavorite(id);
    }

    @GetMapping("/{id}/favorite/status")
    public DataResponse getFavoriteStatus(@PathVariable Long id) {
        return bbsPostService.getFavoriteStatus(id);
    }
}
