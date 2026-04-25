package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsCommentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/comment")
public class BbsCommentController {

    private final BbsCommentService bbsCommentService;

    public BbsCommentController(BbsCommentService bbsCommentService) {
        this.bbsCommentService = bbsCommentService;
    }

    @GetMapping("/post/{postId}")
    public DataResponse getCommentsByPost(@PathVariable Long postId) {
        return bbsCommentService.getCommentsByPost(postId);
    }

    @GetMapping("/detail/{id}")
    public DataResponse getCommentDetail(@PathVariable Long id) {
        return bbsCommentService.getCommentDetail(id);
    }

    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse createComment(@PathVariable Long postId, @Valid @RequestBody DataRequest dataRequest) {
        return bbsCommentService.createComment(postId, dataRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse updateComment(@PathVariable Long id, @Valid @RequestBody DataRequest dataRequest) {
        return bbsCommentService.updateComment(id, dataRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse deleteComment(@PathVariable Long id) {
        return bbsCommentService.deleteComment(id);
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public DataResponse toggleLike(@PathVariable Long id) {
        return bbsCommentService.toggleLike(id);
    }

    @GetMapping("/{id}/like/status")
    public DataResponse getLikeStatus(@PathVariable Long id) {
        return bbsCommentService.getLikeStatus(id);
    }
}
