package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Feedback;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.FeedbackRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DataResponse submitFeedback(Integer userId, String type, String title, String description, String contact) {
        if (userId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }
        if (type == null || type.isBlank()) {
            return CommonMethod.getReturnMessageError("反馈类型不能为空");
        }
        if (title == null || title.isBlank()) {
            return CommonMethod.getReturnMessageError("反馈标题不能为空");
        }
        if (title.length() > 100) {
            return CommonMethod.getReturnMessageError("反馈标题长度不能超过100");
        }
        if (description == null || description.isBlank()) {
            return CommonMethod.getReturnMessageError("反馈描述不能为空");
        }
        if (description.length() > 2000) {
            return CommonMethod.getReturnMessageError("反馈描述长度不能超过2000");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setTitle(title);
        feedback.setDescription(description);
        feedback.setContact(contact);
        feedback.setStatus("PENDING");

        feedbackRepository.saveAndFlush(feedback);

        fillUserInfo(feedback);

        return CommonMethod.getReturnData(feedback);
    }

    public DataResponse getFeedbackList() {
        List<Feedback> feedbackList = feedbackRepository.findAllByOrderByCreateTimeDesc();
        feedbackList.forEach(this::fillUserInfo);
        return CommonMethod.getReturnData(feedbackList);
    }

    @Transactional
    public DataResponse handleFeedback(Long id, String result) {
        if (id == null) {
            return CommonMethod.getReturnMessageError("反馈ID不能为空");
        }
        if (result == null || result.isBlank()) {
            return CommonMethod.getReturnMessageError("处理结果不能为空");
        }
        if (result.length() > 500) {
            return CommonMethod.getReturnMessageError("处理结果长度不能超过500");
        }

        Optional<Feedback> feedbackOptional = feedbackRepository.findById(id);
        if (feedbackOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("反馈不存在");
        }

        Feedback feedback = feedbackOptional.get();
        feedback.setStatus("HANDLED");
        feedback.setHandleResult(result);
        feedbackRepository.saveAndFlush(feedback);

        fillUserInfo(feedback);

        return CommonMethod.getReturnData(feedback);
    }

    public DataResponse findByUserId(Integer userId) {
        List<Feedback> feedbackList = feedbackRepository.findByUserIdOrderByCreateTimeDesc(userId);
        feedbackList.forEach(this::fillUserInfo);
        return CommonMethod.getReturnData(feedbackList);
    }

    private void fillUserInfo(Feedback feedback) {
        if (feedback.getUserId() != null) {
            Optional<User> userOptional = userRepository.findById(feedback.getUserId());
            if (userOptional.isPresent()) {
                feedback.setUserNickname(userOptional.get().getNickname());
            }
        }
    }
}
