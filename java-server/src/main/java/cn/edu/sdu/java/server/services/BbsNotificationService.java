package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsNotificationRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BbsNotificationService {

    private final BbsNotificationRepository bbsNotificationRepository;

    public BbsNotificationService(BbsNotificationRepository bbsNotificationRepository) {
        this.bbsNotificationRepository = bbsNotificationRepository;
    }

    public DataResponse getUnreadCount() {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Long count = bbsNotificationRepository.countByReceiverIdAndIsRead(
            currentUserId.longValue(), 0);
        
        return CommonMethod.getReturnData(count);
    }

    public DataResponse getMyNotificationList(DataRequest dataRequest) {
        Integer isRead = dataRequest.getInteger("isRead");

        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        List<BbsNotification> notificationList;
        
        if (isRead != null) {
            notificationList = bbsNotificationRepository.findByReceiverIdAndIsReadOrderByCreateTimeDesc(
                currentUserId.longValue(), isRead);
        } else {
            notificationList = bbsNotificationRepository.findByReceiverIdOrderByCreateTimeDesc(
                currentUserId.longValue());
        }

        return CommonMethod.getReturnData(notificationList);
    }

    @Transactional
    public DataResponse markAsRead(Long id) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsNotification> notificationOptional = bbsNotificationRepository.findById(id);
        if (notificationOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("通知不存在");
        }

        BbsNotification notification = notificationOptional.get();
        if (!notification.getReceiverId().equals(currentUserId.longValue())) {
            return CommonMethod.getReturnMessageError("无权操作此通知");
        }

        notification.setIsRead(1);
        bbsNotificationRepository.saveAndFlush(notification);

        return CommonMethod.getReturnMessageOK("标记已读成功");
    }
}
