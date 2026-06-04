package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsBanAppeal;
import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.models.EUserType;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsBanAppealRepository;
import cn.edu.sdu.java.server.repositorys.BbsNotificationRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BbsBanAppealService {

    private final BbsBanAppealRepository bbsBanAppealRepository;
    private final BbsNotificationRepository bbsNotificationRepository;
    private final UserRepository userRepository;

    public BbsBanAppealService(BbsBanAppealRepository bbsBanAppealRepository,
                               BbsNotificationRepository bbsNotificationRepository,
                               UserRepository userRepository) {
        this.bbsBanAppealRepository = bbsBanAppealRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DataResponse submitAppeal(String reason) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        BbsBanAppeal appeal = new BbsBanAppeal();
        appeal.setUserId(currentUserId.longValue());
        appeal.setReason(reason);
        appeal.setStatus(0);
        
        BbsBanAppeal savedAppeal = bbsBanAppealRepository.saveAndFlush(appeal);
        
        // 向所有admin和super用户发送通知
        sendAppealNotification(savedAppeal);
        
        return CommonMethod.getReturnData(savedAppeal.getId(), "申诉提交成功");
    }

    private void sendAppealNotification(BbsBanAppeal appeal) {
        List<User> admins = userRepository.findAdmins();
        Optional<User> userOpt = userRepository.findById(appeal.getUserId().intValue());
        String userName = userOpt.map(User::getNickname).orElse("未知用户");
        
        for (User admin : admins) {
            BbsNotification notification = new BbsNotification();
            notification.setReceiverId(admin.getPersonId().longValue());
            notification.setType(10); // 申诉通知类型
            notification.setTitle("新的禁言申诉");
            notification.setContent("用户【" + userName + "】提交了禁言申诉，请及时处理。申诉ID：" + appeal.getId());
            bbsNotificationRepository.saveAndFlush(notification);
        }
    }

    public DataResponse getMyAppeals(DataRequest dataRequest) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Integer pageNum = dataRequest.getInteger("pageNum") != null ? dataRequest.getInteger("pageNum") : 1;
        Integer pageSize = dataRequest.getInteger("pageSize") != null ? dataRequest.getInteger("pageSize") : 10;

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<BbsBanAppeal> appealPage = bbsBanAppealRepository.findByUserIdOrderByCreateTimeDesc(
            currentUserId.longValue(), pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("list", appealPage.getContent());
        result.put("total", appealPage.getTotalElements());
        result.put("totalPages", appealPage.getTotalPages());
        result.put("currentPage", pageNum);

        return CommonMethod.getReturnData(result);
    }

    public DataResponse getAllAppeals(DataRequest dataRequest) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }
        
        // 获取当前用户角色
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        if (currentUserOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }
        User currentUser = currentUserOpt.get();
        String currentUserRole = currentUser.getUserType() != null ? currentUser.getUserType().getName() : "";
        
        Integer pageNum = dataRequest.getInteger("pageNum") != null ? dataRequest.getInteger("pageNum") : 1;
        Integer pageSize = dataRequest.getInteger("pageSize") != null ? dataRequest.getInteger("pageSize") : 10;
        Integer status = dataRequest.getInteger("status");

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<BbsBanAppeal> appealPage;

        // 根据用户角色获取可见的申诉
        if (EUserType.ROLE_SUPER.name().equals(currentUserRole)) {
            // 超级管理员可以看到所有申诉
            if (status != null) {
                appealPage = bbsBanAppealRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
            } else {
                appealPage = bbsBanAppealRepository.findAllByOrderByCreateTimeDesc(pageable);
            }
        } else {
            // 普通管理员只能看到普通用户的申诉，看不到admin的申诉
            if (status != null) {
                appealPage = bbsBanAppealRepository.findByStatusAndNonAdminUserOrderByCreateTimeDesc(status, pageable);
            } else {
                appealPage = bbsBanAppealRepository.findByNonAdminUserOrderByCreateTimeDesc(pageable);
            }
        }

        // 为每个申诉添加用户信息
        List<Map<String, Object>> appealList = new ArrayList<>();
        for (BbsBanAppeal appeal : appealPage.getContent()) {
            Map<String, Object> appealMap = new HashMap<>();
            appealMap.put("id", appeal.getId());
            appealMap.put("userId", appeal.getUserId());
            appealMap.put("reason", appeal.getReason());
            appealMap.put("status", appeal.getStatus());
            appealMap.put("handleResult", appeal.getHandleResult());
            appealMap.put("createTime", appeal.getCreateTime());
            appealMap.put("handleTime", appeal.getHandleTime());

            // 加载申诉用户信息
            Optional<User> userOpt = userRepository.findById(appeal.getUserId().intValue());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getPersonId());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("studentId", user.getStudentId());
                userInfo.put("isBanned", user.getIsBanned());
                userInfo.put("role", user.getUserType() != null ? user.getUserType().getName() : "");
                appealMap.put("userInfo", userInfo);
            }

            appealList.add(appealMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", appealList);
        result.put("total", appealPage.getTotalElements());
        result.put("totalPages", appealPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("currentUserRole", currentUserRole);

        return CommonMethod.getReturnData(result);
    }

    public DataResponse getAppealDetail(Long id) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }
        
        // 获取当前用户角色
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        if (currentUserOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }
        User currentUser = currentUserOpt.get();
        String currentUserRole = currentUser.getUserType() != null ? currentUser.getUserType().getName() : "";
        
        Optional<BbsBanAppeal> appealOpt = bbsBanAppealRepository.findById(id);
        if (appealOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("申诉不存在");
        }

        BbsBanAppeal appeal = appealOpt.get();
        
        // 权限检查：如果不是super用户，不能查看admin的申诉
        if (!EUserType.ROLE_SUPER.name().equals(currentUserRole)) {
            Optional<User> appealUserOpt = userRepository.findById(appeal.getUserId().intValue());
            if (appealUserOpt.isPresent()) {
                User appealUser = appealUserOpt.get();
                String appealUserRole = appealUser.getUserType() != null ? appealUser.getUserType().getName() : "";
                if (EUserType.ROLE_ADMIN.name().equals(appealUserRole) || EUserType.ROLE_SUPER.name().equals(appealUserRole)) {
                    return CommonMethod.getReturnMessageError("您没有权限查看此申诉");
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("appeal", appeal);

        // 加载申诉用户信息
        Optional<User> userOpt = userRepository.findById(appeal.getUserId().intValue());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getPersonId());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("studentId", user.getStudentId());
            userInfo.put("isBanned", user.getIsBanned());
            result.put("user", userInfo);
        }

        return CommonMethod.getReturnData(result);
    }

    @Transactional
    public DataResponse handleAppeal(Long id, Integer decision, String handleResult) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsBanAppeal> appealOpt = bbsBanAppealRepository.findById(id);
        if (appealOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("申诉不存在");
        }

        BbsBanAppeal appeal = appealOpt.get();
        if (appeal.getStatus() != 0) {
            return CommonMethod.getReturnMessageError("该申诉已处理");
        }

        // 获取当前用户角色
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        if (currentUserOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }
        User currentUser = currentUserOpt.get();
        String currentUserRole = currentUser.getUserType() != null ? currentUser.getUserType().getName() : "";
        
        // 获取申诉用户角色
        Optional<User> appealUserOpt = userRepository.findById(appeal.getUserId().intValue());
        if (appealUserOpt.isEmpty()) {
            return CommonMethod.getReturnMessageError("申诉用户不存在");
        }
        User appealUser = appealUserOpt.get();
        String appealUserRole = appealUser.getUserType() != null ? appealUser.getUserType().getName() : "";
        
        // 权限检查：普通管理员不能处理admin的申诉
        if (EUserType.ROLE_ADMIN.name().equals(currentUserRole)) {
            if (EUserType.ROLE_ADMIN.name().equals(appealUserRole) || EUserType.ROLE_SUPER.name().equals(appealUserRole)) {
                return CommonMethod.getReturnMessageError("您没有权限处理此申诉");
            }
        }

        appeal.setStatus(decision == 1 ? 1 : 2);
        appeal.setHandlerId(currentUserId.longValue());
        appeal.setHandleResult(handleResult);
        appeal.setHandleTime(cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date()));
        
        bbsBanAppealRepository.saveAndFlush(appeal);

        // 如果是同意，解禁用户
        if (decision == 1) {
            appealUser.setIsBanned(false);
            userRepository.saveAndFlush(appealUser);
        }

        // 发送处理结果通知给申诉用户
        sendResultNotification(appeal, decision);

        return CommonMethod.getReturnMessageOK("处理成功");
    }

    private void sendResultNotification(BbsBanAppeal appeal, Integer decision) {
        BbsNotification notification = new BbsNotification();
        notification.setReceiverId(appeal.getUserId());
        notification.setType(10);
        notification.setTitle("申诉处理结果");
        String resultText = decision == 1 ? "已通过，您的账号已解禁" : "已驳回";
        notification.setContent("您的禁言申诉" + resultText + "。处理结果：" + appeal.getHandleResult());
        bbsNotificationRepository.saveAndFlush(notification);
    }
}
