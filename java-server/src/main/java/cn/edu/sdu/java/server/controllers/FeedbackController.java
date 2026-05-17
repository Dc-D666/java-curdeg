package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.FeedbackService;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public DataResponse submitFeedback(@RequestBody DataRequest dataRequest) {
        Integer userId = CommonMethod.getPersonId();
        String type = dataRequest.getString("type");
        String title = dataRequest.getString("title");
        String description = dataRequest.getString("description");
        String contact = dataRequest.getString("contact");
        return feedbackService.submitFeedback(userId, type, title, description, contact);
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyFeedbackList() {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }
        return CommonMethod.getReturnData(feedbackService.findByUserId(userId));
    }
}
