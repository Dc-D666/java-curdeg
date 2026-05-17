package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public DataResponse getFeedbackList() {
        return feedbackService.getFeedbackList();
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public DataResponse handleFeedback(@PathVariable Long id, @RequestBody DataRequest dataRequest) {
        String result = dataRequest.getString("result");
        return feedbackService.handleFeedback(id, result);
    }
}
