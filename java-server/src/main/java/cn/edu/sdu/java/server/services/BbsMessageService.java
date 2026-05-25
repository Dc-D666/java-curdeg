package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsConversation;
import cn.edu.sdu.java.server.models.BbsMessage;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsConversationRepository;
import cn.edu.sdu.java.server.repositorys.BbsFollowRepository;
import cn.edu.sdu.java.server.repositorys.BbsMessageRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BbsMessageService {

    private static final String DEFAULT_AVATAR_URL =
            "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";

    private final BbsConversationRepository conversationRepository;
    private final BbsMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BbsFollowRepository followRepository;
    private final ObjectMapper objectMapper;
    private final LevelPrivilegeService levelPrivilegeService;

    public BbsMessageService(BbsConversationRepository conversationRepository,
                             BbsMessageRepository messageRepository,
                             UserRepository userRepository,
                             BbsFollowRepository followRepository,
                             ObjectMapper objectMapper,
                             LevelPrivilegeService levelPrivilegeService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.objectMapper = objectMapper;
        this.levelPrivilegeService = levelPrivilegeService;
    }

    private void fillUserBasicInfo(Map<String, Object> userMap, User user) {
        userMap.put("userId", user.getPersonId());
        userMap.put("nickname", user.getNickname());
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isBlank()) {
            avatarUrl = DEFAULT_AVATAR_URL;
        }
        userMap.put("avatarUrl", avatarUrl);
    }

    public DataResponse getConversationList() {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        List<BbsConversation> conversations =
                conversationRepository.findByUserIdOrderByLastMessageTimeDesc(currentUserId);
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (BbsConversation conversation : conversations) {
            Map<String, Object> convMap = new HashMap<>();
            convMap.put("conversationId", conversation.getId());
            convMap.put("lastMessageTime", conversation.getLastMessageTime());

            Integer otherUserId = conversation.getUser1Id().equals(currentUserId)
                    ? conversation.getUser2Id()
                    : conversation.getUser1Id();
            convMap.put("otherUserId", otherUserId);

            Optional<User> otherUserOpt = userRepository.findById(otherUserId);
            if (otherUserOpt.isEmpty()) {
                continue;
            }

            User otherUser = otherUserOpt.get();
            Map<String, Object> userMap = new HashMap<>();
            fillUserBasicInfo(userMap, otherUser);
            convMap.put("otherUser", userMap);

            Integer unreadCount = conversation.getUser1Id().equals(currentUserId)
                    ? conversation.getUser1UnreadCount()
                    : conversation.getUser2UnreadCount();
            convMap.put("unreadCount", unreadCount);

            if (conversation.getLastMessageId() != null) {
                Optional<BbsMessage> lastMessageOpt = messageRepository.findById(conversation.getLastMessageId());
                if (lastMessageOpt.isPresent()) {
                    BbsMessage lastMessage = lastMessageOpt.get();
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("messageId", lastMessage.getId());
                    msgMap.put("messageType", lastMessage.getMessageType());
                    msgMap.put("content", lastMessage.getContent());
                    msgMap.put("imageUrl", resolveMessageImageUrl(lastMessage));
                    msgMap.put("senderId", lastMessage.getSenderId());
                    msgMap.put("createTime", lastMessage.getCreateTime());
                    convMap.put("lastMessage", msgMap);
                }
            }

            resultList.add(convMap);
        }

        return CommonMethod.getReturnData(resultList);
    }

    @Transactional
    public DataResponse getOrCreateConversation(Integer otherUserId) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }
        if (currentUserId.equals(otherUserId)) {
            return CommonMethod.getReturnMessageError("不能和自己聊天");
        }

        Optional<User> otherUserOpt = userRepository.findById(otherUserId);
        if (otherUserOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        Optional<BbsConversation> existingConv = conversationRepository.findByUserPair(
                Math.min(currentUserId, otherUserId),
                Math.max(currentUserId, otherUserId)
        );

        BbsConversation conversation;
        if (existingConv.isPresent()) {
            conversation = existingConv.get();
        } else {
            conversation = new BbsConversation();
            conversation.setUser1Id(Math.min(currentUserId, otherUserId));
            conversation.setUser2Id(Math.max(currentUserId, otherUserId));
            conversation.setUser1UnreadCount(0);
            conversation.setUser2UnreadCount(0);
            conversation = conversationRepository.saveAndFlush(conversation);
        }

        User otherUser = otherUserOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversation.getId());
        result.put("otherUserId", otherUserId);

        Map<String, Object> userMap = new HashMap<>();
        fillUserBasicInfo(userMap, otherUser);
        result.put("otherUser", userMap);
        result.put("isMutualFollow", checkMutualFollow(currentUserId, otherUserId));

        return CommonMethod.getReturnData(result);
    }

    public DataResponse getMessageHistory(Long conversationId) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsConversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("会话不存在");
        }

        BbsConversation conversation = convOpt.get();
        if (!conversation.getUser1Id().equals(currentUserId) && !conversation.getUser2Id().equals(currentUserId)) {
            return CommonMethod.getReturnMessageError("无权访问该会话");
        }

        List<BbsMessage> messages = messageRepository.findByConversationIdOrderByCreateTimeAsc(conversationId);
        List<Map<String, Object>> messageList = new ArrayList<>();

        for (BbsMessage msg : messages) {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("messageId", msg.getId());
            msgMap.put("conversationId", msg.getConversationId());
            msgMap.put("senderId", msg.getSenderId());
            msgMap.put("receiverId", msg.getReceiverId());
            msgMap.put("messageType", msg.getMessageType());
            msgMap.put("content", msg.getContent());
            msgMap.put("imageUrl", resolveMessageImageUrl(msg));
            msgMap.put("isRead", msg.getIsRead());
            msgMap.put("createTime", msg.getCreateTime());
            msgMap.put("isOwnMessage", msg.getSenderId().equals(currentUserId));
            messageList.add(msgMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversationId);

        Integer otherUserId = conversation.getUser1Id().equals(currentUserId)
                ? conversation.getUser2Id()
                : conversation.getUser1Id();
        result.put("otherUserId", otherUserId);

        Optional<User> otherUserOpt = userRepository.findById(otherUserId);
        if (otherUserOpt.isPresent()) {
            Map<String, Object> userMap = new HashMap<>();
            fillUserBasicInfo(userMap, otherUserOpt.get());
            result.put("otherUser", userMap);
        }

        result.put("isMutualFollow", checkMutualFollow(currentUserId, otherUserId));
        result.put("messages", messageList);

        return CommonMethod.getReturnData(result);
    }

    @Transactional
    public DataResponse sendMessage(Long conversationId, String messageType, String content) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsConversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("会话不存在");
        }

        BbsConversation conversation = convOpt.get();
        if (!conversation.getUser1Id().equals(currentUserId) && !conversation.getUser2Id().equals(currentUserId)) {
            return CommonMethod.getReturnMessageError("无权限发送消息");
        }

        Integer otherUserId = conversation.getUser1Id().equals(currentUserId)
                ? conversation.getUser2Id()
                : conversation.getUser1Id();

        boolean isMutualFollow = checkMutualFollow(currentUserId, otherUserId);
        if (!isMutualFollow) {
            Optional<User> currentUserOpt = userRepository.findById(currentUserId);
            int messageLimit = 1;
            if (currentUserOpt.isPresent()) {
                messageLimit = levelPrivilegeService.getPrivateMessageLimit(currentUserOpt.get().getLevel());
            }

            if (messageLimit <= 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("canSend", false);
                result.put("message", "您当前等级不具备私信权限");
                return CommonMethod.getReturnData(result);
            }

            long existingMessages = messageRepository.countMessagesBetweenUsers(currentUserId, otherUserId);
            if (existingMessages >= messageLimit) {
                Map<String, Object> result = new HashMap<>();
                result.put("canSend", false);
                boolean currentUserFollowsOther =
                        followRepository.existsByFollowerIdAndFollowingId(currentUserId, otherUserId);
                boolean otherFollowsCurrentUser =
                        followRepository.existsByFollowerIdAndFollowingId(otherUserId, currentUserId);

                if (currentUserFollowsOther && !otherFollowsCurrentUser) {
                    result.put("message", "对方还没有关注你，你只能发送 " + messageLimit + " 条消息");
                } else if (!currentUserFollowsOther && otherFollowsCurrentUser) {
                    result.put("message", "你还没有回关对方，只能发送 " + messageLimit + " 条消息");
                } else {
                    result.put("message", "你们还没有互相关注，只能发送 " + messageLimit + " 条消息");
                }
                return CommonMethod.getReturnData(result);
            }
        }

        BbsMessage message = new BbsMessage();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setReceiverId(otherUserId);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setContent(content);

        if (content != null && content.contains("\"imageUrl\"")) {
            try {
                Map<String, Object> json = objectMapper.readValue(content, Map.class);
                if (json.get("imageUrl") != null) {
                    message.setImageUrl(String.valueOf(json.get("imageUrl")));
                }
                if (json.get("text") != null) {
                    message.setContent(String.valueOf(json.get("text")));
                }
            } catch (Exception ignored) {
                // keep original content when mixed-content parsing fails
            }
        }

        if (message.getImageUrl() == null && (isUploadUrl(content) || "image".equalsIgnoreCase(message.getMessageType()))) {
            message.setImageUrl(content);
        }

        message.setIsRead(false);
        message = messageRepository.saveAndFlush(message);

        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageTime(message.getCreateTime());
        if (conversation.getUser1Id().equals(currentUserId)) {
            conversation.setUser2UnreadCount(conversation.getUser2UnreadCount() + 1);
        } else {
            conversation.setUser1UnreadCount(conversation.getUser1UnreadCount() + 1);
        }
        conversationRepository.saveAndFlush(conversation);

        Map<String, Object> result = new HashMap<>();
        result.put("canSend", true);
        result.put("messageId", message.getId());
        result.put("conversationId", conversationId);
        result.put("createTime", message.getCreateTime());
        return CommonMethod.getReturnData(result);
    }

    @Transactional
    public DataResponse markAsRead(Long conversationId) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsConversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("会话不存在");
        }

        BbsConversation conversation = convOpt.get();
        if (!conversation.getUser1Id().equals(currentUserId) && !conversation.getUser2Id().equals(currentUserId)) {
            return CommonMethod.getReturnMessageError("无权限操作该会话");
        }

        messageRepository.markAllAsRead(conversationId, currentUserId);
        if (conversation.getUser1Id().equals(currentUserId)) {
            conversation.setUser1UnreadCount(0);
        } else {
            conversation.setUser2UnreadCount(0);
        }
        conversationRepository.saveAndFlush(conversation);

        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversationId);
        result.put("read", true);
        result.put("unreadCount", 0);
        return CommonMethod.getReturnData(result);
    }

    public DataResponse getUnreadCount() {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        long unreadConversations = conversationRepository.countUnreadConversations(currentUserId);
        List<BbsConversation> conversations = conversationRepository.findByUserIdOrderByLastMessageTimeDesc(currentUserId);
        int totalUnread = 0;
        for (BbsConversation conversation : conversations) {
            if (conversation.getUser1Id().equals(currentUserId)) {
                totalUnread += conversation.getUser1UnreadCount();
            } else {
                totalUnread += conversation.getUser2UnreadCount();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("unreadConversations", unreadConversations);
        result.put("totalUnread", totalUnread);
        return CommonMethod.getReturnData(result);
    }

    private String resolveMessageImageUrl(BbsMessage message) {
        if (message == null) {
            return null;
        }
        if (isUploadUrl(message.getImageUrl())) {
            return message.getImageUrl();
        }
        if (isUploadUrl(message.getContent())) {
            return message.getContent();
        }
        return null;
    }

    private boolean isUploadUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.startsWith("/uploads/")
                || trimmed.startsWith("http://")
                || trimmed.startsWith("https://");
    }

    private boolean checkMutualFollow(Integer userId1, Integer userId2) {
        boolean user1FollowsUser2 = followRepository.existsByFollowerIdAndFollowingId(userId1, userId2);
        boolean user2FollowsUser1 = followRepository.existsByFollowerIdAndFollowingId(userId2, userId1);
        return user1FollowsUser2 && user2FollowsUser1;
    }
}
