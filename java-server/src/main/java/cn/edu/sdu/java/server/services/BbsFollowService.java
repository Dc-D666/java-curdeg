package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsFollow;
import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsFollowRepository;
import cn.edu.sdu.java.server.repositorys.BbsNotificationRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BbsFollowService {

    private final BbsFollowRepository bbsFollowRepository;
    private final UserRepository userRepository;
    private final BbsNotificationRepository bbsNotificationRepository;
    private final PointService pointService;

    public BbsFollowService(BbsFollowRepository bbsFollowRepository, UserRepository userRepository, BbsNotificationRepository bbsNotificationRepository, PointService pointService) {
        this.bbsFollowRepository = bbsFollowRepository;
        this.userRepository = userRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.pointService = pointService;
    }

    private void fillUserInfo(Map<String, Object> userMap, User user) {
        userMap.put("userId", user.getPersonId());
        userMap.put("nickname", user.getNickname());
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isBlank()) {
            avatarUrl = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";
        }
        userMap.put("avatarUrl", avatarUrl);
        userMap.put("signature", user.getSignature());
        userMap.put("postCount", user.getPostCount());
        userMap.put("commentCount", user.getCommentCount());
        userMap.put("followerCount", user.getFollowerCount());
        userMap.put("followingCount", user.getFollowingCount());
        userMap.put("isBanned", user.getIsBanned());
    }

    @Transactional
    public DataResponse toggleFollow(Integer followingId) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        if (currentUserId.equals(followingId)) {
            return CommonMethod.getReturnMessageError("不能关注自己");
        }

        Optional<User> currentUserOptional = userRepository.findById(currentUserId);
        if (currentUserOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("当前用户不存在");
        }
        User currentUser = currentUserOptional.get();

        boolean alreadyFollowed = bbsFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, followingId);

        if (alreadyFollowed) {
            bbsFollowRepository.deleteByFollowerIdAndFollowingId(currentUserId, followingId);
            userRepository.updateFollowingCount(currentUserId, -1);
            userRepository.updateFollowerCount(followingId, -1);
        } else {
            BbsFollow follow = new BbsFollow();
            follow.setFollowerId(currentUserId);
            follow.setFollowingId(followingId);
            bbsFollowRepository.saveAndFlush(follow);
            userRepository.updateFollowingCount(currentUserId, 1);

            BbsNotification notification = new BbsNotification();
            notification.setReceiverId(followingId.longValue());
            notification.setType(5);
            notification.setTitle("新增粉丝通知");
            notification.setContent("用户【" + currentUser.getNickname() + "】关注了你");
            bbsNotificationRepository.saveAndFlush(notification);

            // 被关注积分奖励 - 使用原生SQL更新follower_count避免乐观锁冲突
            pointService.addPoints(followingId, "RECEIVED_FOLLOW", "被关注", follow.getId(), "FOLLOW");
            userRepository.updateFollowerCount(followingId, 1);
        }

        // 获取最新的计数
        Integer followingCount = userRepository.findById(currentUserId)
                .map(User::getFollowingCount)
                .orElse(0);
        Integer followerCount = userRepository.findById(followingId)
                .map(User::getFollowerCount)
                .orElse(0);

        Map<String, Object> result = new HashMap<>();
        result.put("followed", !alreadyFollowed);
        result.put("followingCount", followingCount);
        result.put("followerCount", followerCount);

        return CommonMethod.getReturnData(result);
    }

    public DataResponse checkFollowStatus(Integer followingId) {
        Integer currentUserId = CommonMethod.getPersonId();
        boolean followed = false;

        if (currentUserId != null) {
            followed = bbsFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, followingId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("followed", followed);

        return CommonMethod.getReturnData(result);
    }

    public DataResponse getFollowingList(Integer userId) {
        List<BbsFollow> followList = bbsFollowRepository.findByFollowerId(userId);
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (BbsFollow follow : followList) {
            Optional<User> userOptional = userRepository.findById(follow.getFollowingId());
            if (userOptional.isPresent()) {
                Map<String, Object> userMap = new HashMap<>();
                fillUserInfo(userMap, userOptional.get());
                userMap.put("followTime", follow.getCreateTime());
                resultList.add(userMap);
            }
        }

        return CommonMethod.getReturnData(resultList);
    }

    public DataResponse getFollowerList(Integer userId) {
        List<BbsFollow> followList = bbsFollowRepository.findByFollowingId(userId);
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (BbsFollow follow : followList) {
            Optional<User> userOptional = userRepository.findById(follow.getFollowerId());
            if (userOptional.isPresent()) {
                Map<String, Object> userMap = new HashMap<>();
                fillUserInfo(userMap, userOptional.get());
                userMap.put("followTime", follow.getCreateTime());
                
                Integer currentUserId = CommonMethod.getPersonId();
                if (currentUserId != null) {
                    boolean isFollowed = bbsFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, follow.getFollowerId());
                    userMap.put("isFollowed", isFollowed);
                } else {
                    userMap.put("isFollowed", false);
                }
                
                resultList.add(userMap);
            }
        }

        return CommonMethod.getReturnData(resultList);
    }

    public DataResponse getFollowingCount(Integer userId) {
        long count = bbsFollowRepository.countByFollowerId(userId);
        return CommonMethod.getReturnData(count);
    }

    public DataResponse getFollowerCount(Integer userId) {
        long count = bbsFollowRepository.countByFollowingId(userId);
        return CommonMethod.getReturnData(count);
    }

    public DataResponse getFollowingListPage(Integer userId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BbsFollow> followPage = bbsFollowRepository.findByFollowerId(userId, pageable);
        
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (BbsFollow follow : followPage.getContent()) {
            Optional<User> userOptional = userRepository.findById(follow.getFollowingId());
            if (userOptional.isPresent()) {
                Map<String, Object> userMap = new HashMap<>();
                fillUserInfo(userMap, userOptional.get());
                userMap.put("followTime", follow.getCreateTime());
                resultList.add(userMap);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultList);
        result.put("total", followPage.getTotalElements());
        result.put("totalPages", followPage.getTotalPages());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return CommonMethod.getReturnData(result);
    }

    public DataResponse getFollowerListPage(Integer userId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BbsFollow> followPage = bbsFollowRepository.findByFollowingId(userId, pageable);
        
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (BbsFollow follow : followPage.getContent()) {
            Optional<User> userOptional = userRepository.findById(follow.getFollowerId());
            if (userOptional.isPresent()) {
                Map<String, Object> userMap = new HashMap<>();
                fillUserInfo(userMap, userOptional.get());
                userMap.put("followTime", follow.getCreateTime());
                
                Integer currentUserId = CommonMethod.getPersonId();
                if (currentUserId != null) {
                    boolean isFollowed = bbsFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, follow.getFollowerId());
                    userMap.put("isFollowed", isFollowed);
                } else {
                    userMap.put("isFollowed", false);
                }
                
                resultList.add(userMap);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultList);
        result.put("total", followPage.getTotalElements());
        result.put("totalPages", followPage.getTotalPages());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return CommonMethod.getReturnData(result);
    }
}
