package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsMessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/message")
public class BbsMessageController {

    private final BbsMessageService messageService;

    public BbsMessageController(BbsMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getConversationList() {
        return messageService.getConversationList();
    }

    @GetMapping("/conversation/{otherUserId}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getOrCreateConversation(@PathVariable Integer otherUserId) {
        return messageService.getOrCreateConversation(otherUserId);
    }

    @GetMapping("/history/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMessageHistory(@PathVariable Long conversationId) {
        return messageService.getMessageHistory(conversationId);
    }

    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public DataResponse sendMessage(@RequestBody Map<String, Object> request) {
        Long conversationId = Long.valueOf(request.get("conversationId").toString());
        String messageType = (String) request.getOrDefault("messageType", "text");
        String content = (String) request.get("content");
        return messageService.sendMessage(conversationId, messageType, content);
    }

    @PostMapping("/read/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse markAsRead(@PathVariable Long conversationId) {
        return messageService.markAsRead(conversationId);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getUnreadCount() {
        return messageService.getUnreadCount();
    }
}
