package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsNotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/notification")
public class BbsNotificationController {

    private final BbsNotificationService bbsNotificationService;

    public BbsNotificationController(BbsNotificationService bbsNotificationService) {
        this.bbsNotificationService = bbsNotificationService;
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getUnreadCount() {
        return bbsNotificationService.getUnreadCount();
    }

    @GetMapping("/my-list")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyNotificationList(@RequestParam Map<String, String> params) {
        DataRequest dataRequest = new DataRequest();
        params.forEach((key, value) -> {
            try {
                if ("isRead".equals(key)) {
                    dataRequest.add(key, Integer.parseInt(value));
                } else {
                    dataRequest.add(key, value);
                }
            } catch (Exception e) {
                dataRequest.add(key, value);
            }
        });
        return bbsNotificationService.getMyNotificationList(dataRequest);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public DataResponse markAsRead(@PathVariable Long id) {
        return bbsNotificationService.markAsRead(id);
    }
}
