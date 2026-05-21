package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BbsUserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final BbsPostRepository bbsPostRepository;
    private final BbsFavoriteRepository bbsFavoriteRepository;
    private final BbsLikeRepository bbsLikeRepository;
    private final BbsCommentRepository bbsCommentRepository;
    private final BbsFollowRepository bbsFollowRepository;
    private final BbsBoardRepository bbsBoardRepository;
    private final EmailVerificationService emailVerificationService;
    private final PointService pointService;
    private final LevelPrivilegeService levelPrivilegeService;

    public BbsUserService(UserRepository userRepository, PersonRepository personRepository,
                          UserTypeRepository userTypeRepository, PasswordEncoder passwordEncoder,
                          BbsPostRepository bbsPostRepository, BbsFavoriteRepository bbsFavoriteRepository,
                          BbsLikeRepository bbsLikeRepository, BbsCommentRepository bbsCommentRepository,
                          BbsFollowRepository bbsFollowRepository, BbsBoardRepository bbsBoardRepository,
                          EmailVerificationService emailVerificationService, PointService pointService,
                          LevelPrivilegeService levelPrivilegeService) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.userTypeRepository = userTypeRepository;
        this.passwordEncoder = passwordEncoder;
        this.bbsPostRepository = bbsPostRepository;
        this.bbsFavoriteRepository = bbsFavoriteRepository;
        this.bbsLikeRepository = bbsLikeRepository;
        this.bbsCommentRepository = bbsCommentRepository;
        this.bbsFollowRepository = bbsFollowRepository;
        this.bbsBoardRepository = bbsBoardRepository;
        this.emailVerificationService = emailVerificationService;
        this.pointService = pointService;
        this.levelPrivilegeService = levelPrivilegeService;
    }

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{6,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^\\d{10,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final String CHANGE_PASSWORD_VERIFICATION_TYPE = "CHANGE_PASSWORD";

    @Transactional
    public DataResponse register(DataRequest dataRequest) {
        String username = dataRequest.getString("username");
        String password = dataRequest.getString("password");
        String studentId = dataRequest.getString("studentId");
        String nickname = dataRequest.getString("nickname");

        if (username == null || username.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：用户名不能为空");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return CommonMethod.getReturnMessageError("参数错误：用户名长度6-20，仅字母数字下划线");
        }

        if (password == null || password.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：密码不能为空");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return CommonMethod.getReturnMessageError("参数错误：密码长度8-20，至少包含字母和数字");
        }

        if (studentId == null || studentId.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：学号不能为空");
        }
        if (!STUDENT_ID_PATTERN.matcher(studentId).matches()) {
            return CommonMethod.getReturnMessageError("参数错误：学号长度10-20，仅数字");
        }

        if (nickname == null || nickname.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：昵称不能为空");
        }
        if (nickname.length() < 2 || nickname.length() > 20) {
            return CommonMethod.getReturnMessageError("参数错误：昵称长度2-20");
        }

        if (userRepository.existsByUserName(username)) {
            return CommonMethod.getReturnMessageError("登录用户名已存在");
        }

        if (userRepository.existsByStudentId(studentId)) {
            return CommonMethod.getReturnMessageError("学号已注册");
        }

        Person person = new Person();
        person.setNum(username);
        person.setName(nickname);
        person.setType("1");
        Person savedPerson = personRepository.saveAndFlush(person);

        Integer personId = savedPerson.getPersonId();

        UserType userType = userTypeRepository.findByName(EUserType.ROLE_STUDENT.name());

        User user = new User();
        user.setPersonId(personId);
        user.setUserType(userType);
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreateTime(DateTimeTool.parseDateTime(new Date()));
        user.setCreatorId(personId);
        user.setLoginCount(0);
        user.setStudentId(studentId);
        user.setNickname(nickname);
        user.setPostCount(0);
        user.setCommentCount(0);
        user.setViolationCount(0);
        user.setIsBanned(false);
        user.setPoints(0);
        user.setLevel(0);

        userRepository.saveAndFlush(user);

        return CommonMethod.getReturnMessageOK("注册成功");
    }

    public DataResponse getCurrentUser() {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<User> userOptional = userRepository.findById(currentUserId);
        if (userOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        User user = userOptional.get();

        // 获取 Person 数据并填充到临时字段
        if (user.getPerson() != null) {
            Person person = user.getPerson();
            user.setPersonName(person.getName());
            user.setPersonDept(person.getDept());
            user.setPersonGender(person.getGender());
            user.setPersonBirthday(person.getBirthday());
            user.setPersonEmail(person.getEmail());
            user.setPersonPhone(person.getPhone());
            user.setPersonAddress(person.getAddress());
            user.setPersonIntroduce(person.getIntroduce());
            
            // 获取并填充隐私设置
            user.setNamePrivacy(person.getNamePrivacy());
            user.setDeptPrivacy(person.getDeptPrivacy());
            user.setGenderPrivacy(person.getGenderPrivacy());
            user.setBirthdayPrivacy(person.getBirthdayPrivacy());
            user.setEmailPrivacy(person.getEmailPrivacy());
            user.setPhonePrivacy(person.getPhonePrivacy());
            user.setAddressPrivacy(person.getAddressPrivacy());
            user.setIntroducePrivacy(person.getIntroducePrivacy());
        }

        if (user.getUserType() != null) {
            user.setAuthority(user.getUserType().getName());
        }

        return CommonMethod.getReturnData(user);
    }

    public User getUserById(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        return userOptional.get();
    }

    private double calculateProfileCompletion(User user) {
        int filled = 0;
        int total = 10;
        if (user.getNickname() != null && !user.getNickname().isBlank()) filled++;
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) filled++;
        if (user.getSignature() != null && !user.getSignature().isBlank()) filled++;
        Person person = user.getPerson();
        if (person != null) {
            if (person.getName() != null && !person.getName().isBlank()) filled++;
            if (person.getDept() != null && !person.getDept().isBlank()) filled++;
            if (person.getGender() != null && !person.getGender().isBlank()) filled++;
            if (person.getBirthday() != null && !person.getBirthday().isBlank()) filled++;
            if (person.getEmail() != null && !person.getEmail().isBlank()) filled++;
            if (person.getPhone() != null && !person.getPhone().isBlank()) filled++;
            if (person.getAddress() != null && !person.getAddress().isBlank()) filled++;
        }
        return (double) filled / total * 100;
    }

    @Transactional
    public DataResponse updateCurrentUser(DataRequest dataRequest) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<User> userOptional = userRepository.findById(currentUserId);
        if (userOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        User user = userOptional.get();

        String nickname = dataRequest.getString("nickname");
        if (nickname != null && !nickname.isBlank()) {
            if (nickname.length() < 2 || nickname.length() > 20) {
                return CommonMethod.getReturnMessageError("参数错误：昵称长度2-20");
            }
            user.setNickname(nickname);
        }

        String avatarUrl = dataRequest.getString("avatarUrl");
        if (avatarUrl != null) {
            if (avatarUrl.length() > 255) {
                return CommonMethod.getReturnMessageError("参数错误：头像URL长度不能超过255");
            }
            user.setAvatarUrl(avatarUrl);
        }

        String signature = dataRequest.getString("signature");
        if (signature != null) {
            if (signature.length() > 200) {
                return CommonMethod.getReturnMessageError("参数错误：个性签名长度不能超过200");
            }
            user.setSignature(signature);
        }

        // 更新 Person 字段
        Person person = user.getPerson();
        if (person != null) {
            String personName = dataRequest.getString("personName");
            if (personName != null) {
                if (personName.length() > 50) {
                    return CommonMethod.getReturnMessageError("参数错误：姓名长度不能超过50");
                }
                person.setName(personName);
            }

            String personDept = dataRequest.getString("personDept");
            if (personDept != null) {
                if (personDept.length() > 50) {
                    return CommonMethod.getReturnMessageError("参数错误：学院长度不能超过50");
                }
                person.setDept(personDept);
            }

            String personGender = dataRequest.getString("personGender");
            if (personGender != null) {
                if (personGender.length() > 2) {
                    return CommonMethod.getReturnMessageError("参数错误：性别长度不能超过2");
                }
                person.setGender(personGender);
            }

            String personBirthday = dataRequest.getString("personBirthday");
            if (personBirthday != null) {
                if (personBirthday.length() > 10) {
                    return CommonMethod.getReturnMessageError("参数错误：生日长度不能超过10");
                }
                person.setBirthday(personBirthday);
            }

            String personEmail = dataRequest.getString("personEmail");
            if (personEmail != null) {
                if (personEmail.length() > 60) {
                    return CommonMethod.getReturnMessageError("参数错误：邮箱长度不能超过60");
                }
                if (!personEmail.isBlank() && !EMAIL_PATTERN.matcher(personEmail).matches()) {
                    return CommonMethod.getReturnMessageError("参数错误：邮箱格式不正确");
                }
                person.setEmail(personEmail);
            }

            String personPhone = dataRequest.getString("personPhone");
            if (personPhone != null) {
                if (personPhone.length() > 20) {
                    return CommonMethod.getReturnMessageError("参数错误：电话长度不能超过20");
                }
                person.setPhone(personPhone);
            }

            String personAddress = dataRequest.getString("personAddress");
            if (personAddress != null) {
                if (personAddress.length() > 20) {
                    return CommonMethod.getReturnMessageError("参数错误：地址长度不能超过20");
                }
                person.setAddress(personAddress);
            }

            String personIntroduce = dataRequest.getString("personIntroduce");
            if (personIntroduce != null) {
                if (personIntroduce.length() > 1000) {
                    return CommonMethod.getReturnMessageError("参数错误：个人简介长度不能超过1000");
                }
                person.setIntroduce(personIntroduce);
            }

            // 更新隐私设置
            String namePrivacy = dataRequest.getString("namePrivacy");
            if (namePrivacy != null) {
                if (!namePrivacy.isBlank() && !namePrivacy.equals("PUBLIC") && !namePrivacy.equals("FOLLOWING") && !namePrivacy.equals("PRIVATE")) {
                    return CommonMethod.getReturnMessageError("参数错误：隐私设置值必须为 PUBLIC, FOLLOWING 或 PRIVATE");
                }
                person.setNamePrivacy(namePrivacy.isBlank() ? "PUBLIC" : namePrivacy);
            }

            String deptPrivacy = dataRequest.getString("deptPrivacy");
            if (deptPrivacy != null) {
                if (!deptPrivacy.isBlank() && !deptPrivacy.equals("PUBLIC") && !deptPrivacy.equals("FOLLOWING") && !deptPrivacy.equals("PRIVATE")) {
                    return CommonMethod.getReturnMessageError("参数错误：隐私设置值必须为 PUBLIC, FOLLOWING 或 PRIVATE");
                }
                person.setDeptPrivacy(deptPrivacy.isBlank() ? "PUBLIC" : deptPrivacy);
            }

            String genderPrivacy = dataRequest.getString("genderPrivacy");
            if (genderPrivacy != null) {
                if (!genderPrivacy.isBlank() && !genderPrivacy.equals("PUBLIC") && !genderPrivacy.equals("FOLLOWING") && !genderPrivacy.equals("PRIVATE")) {
                    return CommonMethod.getReturnMessageError("参数错误：隐私设置值必须为 PUBLIC, FOLLOWING 或 PRIVATE");
                }
                person.setGenderPrivacy(genderPrivacy.isBlank() ? "PUBLIC" : genderPrivacy);
            }

            String birthdayPrivacy = dataRequest.getString("birthdayPrivacy");
            if (birthdayPrivacy != null) {
                if (!birthdayPrivacy.isBlank() && !birthdayPrivacy.equals("PUBLIC") && !birthdayPrivacy.equals("FOLLOWING") && !birthdayPrivacy.equals("PRIVATE")) {
                    return CommonMethod.getReturnMessageError("参数错误：隐私设置值必须为 PUBLIC, FOLLOWING 或 PRIVATE");
                }
                person.setBirthdayPrivacy(birthdayPrivacy.isBlank() ? "PUBLIC" : birthdayPrivacy);
            }

            String emailPrivacy = dataRequest.getString("emailPrivacy");
            if (emailPrivacy != null) {
                if (!emailPrivacy.isBlank() && !emailPrivacy.equals("PUBLIC") && !emailPrivacy.equals("FOLLOWING") && !emailPrivacy.equals("PRIVATE")) {
                    return CommonMethod.getReturnMessageError("参数错误：隐私设置值必须为 PUBLIC, FOLLOWING 或 PRIVATE");
                }
                person.setEmailPrivacy(emailPrivacy.isBlank() ? "PUBLIC" : emailPrivacy);
            }

            String phonePrivacy = dataRequest.getString("phonePrivacy");
            if (phonePrivacy != null) {
                if (!phonePrivacy.isBlank() && !phonePrivacy.equals("PUBLIC") && !phonePrivacy.equals("FOLLOWING") && !phonePrivacy.equals("PRIVATE")) {
                    return CommonMethod.getReturnMessageError("参数错误：隐私设置值必须为 PUBLIC, FOLLOWING 或 PRIVATE");
                }
                person.setPhonePrivacy(phonePrivacy.isBlank() ? "PUBLIC" : phonePrivacy);
            }

            String addressPrivacy = dataRequest.getString("addressPrivacy");
            if (addressPrivacy != null) {
                if (!addressPrivacy.isBlank() && !addressPrivacy.equals("PUBLIC") && !addressPrivacy.equals("FOLLOWING") && !addressPrivacy.equals("PRIVATE")) {
                    return CommonMethod.getReturnMessageError("参数错误：隐私设置值必须为 PUBLIC, FOLLOWING 或 PRIVATE");
                }
                person.setAddressPrivacy(addressPrivacy.isBlank() ? "PUBLIC" : addressPrivacy);
            }

            String introducePrivacy = dataRequest.getString("introducePrivacy");
            if (introducePrivacy != null) {
                if (!introducePrivacy.isBlank() && !introducePrivacy.equals("PUBLIC") && !introducePrivacy.equals("FOLLOWING") && !introducePrivacy.equals("PRIVATE")) {
                    return CommonMethod.getReturnMessageError("参数错误：隐私设置值必须为 PUBLIC, FOLLOWING 或 PRIVATE");
                }
                person.setIntroducePrivacy(introducePrivacy.isBlank() ? "PUBLIC" : introducePrivacy);
            }

            personRepository.saveAndFlush(person);

            // 更新临时字段
            user.setPersonName(person.getName());
            user.setPersonDept(person.getDept());
            user.setPersonGender(person.getGender());
            user.setPersonBirthday(person.getBirthday());
            user.setPersonEmail(person.getEmail());
            user.setPersonPhone(person.getPhone());
            user.setPersonAddress(person.getAddress());
            user.setPersonIntroduce(person.getIntroduce());
            
            // 更新隐私设置临时字段
            user.setNamePrivacy(person.getNamePrivacy());
            user.setDeptPrivacy(person.getDeptPrivacy());
            user.setGenderPrivacy(person.getGenderPrivacy());
            user.setBirthdayPrivacy(person.getBirthdayPrivacy());
            user.setEmailPrivacy(person.getEmailPrivacy());
            user.setPhonePrivacy(person.getPhonePrivacy());
            user.setAddressPrivacy(person.getAddressPrivacy());
            user.setIntroducePrivacy(person.getIntroducePrivacy());
        }

        // 资料完善度检查（先标记，不在此处发放积分）
        boolean needPointReward = calculateProfileCompletion(user) >= 80 && Boolean.FALSE.equals(user.getProfileCompletedReward());

        userRepository.updateProfile(currentUserId,
            user.getNickname(),
            user.getAvatarUrl(),
            user.getSignature()
        );

        if (needPointReward) {
            userRepository.updateProfileCompletedReward(currentUserId, 1);
        }

        // 积分奖励在更新后发放
        if (needPointReward) {
            try {
                pointService.addPoints(currentUserId, "PROFILE_COMPLETE", "完善个人资料", null, null);
            } catch (Exception e) {
                log.error("完善资料积分发放失败: userId={}", currentUserId, e);
            }
        }

        if (user.getUserType() != null) {
            user.setAuthority(user.getUserType().getName());
        }

        return CommonMethod.getReturnData(user);
    }

    public DataResponse getUserList(DataRequest dataRequest) {
        Integer pageNum = dataRequest.getInteger("pageNum");
        Integer pageSize = dataRequest.getInteger("pageSize");
        String keyword = dataRequest.getString("keyword");

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        if (keyword != null && keyword.length() > 50) {
            return CommonMethod.getReturnMessageError("参数错误：搜索关键词长度不能超过50");
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<User> userPage = userRepository.searchUsers(keyword, pageable);

        return CommonMethod.getReturnData(userPage);
    }

    public DataResponse getUserStatistics() {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<User> userOptional = userRepository.findById(currentUserId);
        if (userOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        User user = userOptional.get();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("postCount", user.getPostCount());
        statistics.put("commentCount", user.getCommentCount());

        Integer postLikeCount = bbsPostRepository.sumLikeCountByAuthorId(currentUserId.longValue());
        if (postLikeCount == null) postLikeCount = 0;

        Integer commentLikeCount = bbsCommentRepository.sumLikeCountByAuthorId(currentUserId.longValue());
        if (commentLikeCount == null) commentLikeCount = 0;

        statistics.put("totalLikeCount", postLikeCount + commentLikeCount);

        long totalFavoriteCount = bbsFavoriteRepository.countByUserId(currentUserId);
        statistics.put("totalFavoriteCount", totalFavoriteCount);

        Integer totalViewCount = bbsPostRepository.sumViewCountByAuthorId(currentUserId.longValue());
        if (totalViewCount == null) totalViewCount = 0;
        statistics.put("totalViewCount", totalViewCount);

        statistics.put("followingCount", user.getFollowingCount());
        statistics.put("followerCount", user.getFollowerCount());

        // 添加积分等级相关字段
        statistics.put("points", user.getPoints());
        statistics.put("level", user.getLevel());
        statistics.put("levelName", levelPrivilegeService.getLevelName(user.getLevel()));
        statistics.put("weeklyGrowth", pointService.getWeeklyGrowth(currentUserId));
        statistics.put("pointsToNextLevel", levelPrivilegeService.getPointsToNextLevel(user.getPoints()));
        statistics.put("storeDiscount", levelPrivilegeService.getStoreDiscount(user.getLevel()));

        return CommonMethod.getReturnData(statistics);
    }

    public DataResponse getUserStatisticsDetail() {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<User> userOptional = userRepository.findById(currentUserId);
        if (userOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        User user = userOptional.get();
        Long authorId = currentUserId.longValue();
        int days = 30;

        Map<String, Object> overview = buildUserOverview(user, currentUserId);

        Map<String, Object> trends = new LinkedHashMap<>();
        trends.put("postTrend", toTrendList(bbsPostRepository.countDailyPostTrendByAuthor(authorId, days)));
        trends.put("commentTrend", toTrendList(bbsCommentRepository.countDailyCommentTrendByAuthor(authorId, days)));
        trends.put("receivedLikeTrend", toTrendList(bbsLikeRepository.countDailyReceivedLikeTrendByAuthor(authorId, days)));
        trends.put("favoriteTrend", toTrendList(bbsFavoriteRepository.countDailyReceivedFavoriteTrendByAuthor(authorId, days)));
        trends.put("followTrend", toTrendList(bbsFollowRepository.countDailyFollowerTrendByUser(currentUserId, days)));
        trends.put("followingTrend", toTrendList(bbsFollowRepository.countDailyFollowingTrendByUser(currentUserId, days)));

        Map<String, Object> distribution = new LinkedHashMap<>();
        distribution.put("postStatus", toPostStatusDistribution(bbsPostRepository.countPostsByStatusAndAuthor(authorId)));
        distribution.put("interaction", buildInteractionDistribution(overview));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", overview);
        result.put("trends", trends);
        result.put("distribution", distribution);
        result.put("topPosts", buildTopPostList(bbsPostRepository.findTopPostsByAuthorHeat(authorId, 10)));

        return CommonMethod.getReturnData(result);
    }

    private Map<String, Object> buildUserOverview(User user, Integer currentUserId) {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("postCount", safeInteger(user.getPostCount()));
        overview.put("commentCount", safeInteger(user.getCommentCount()));

        Integer postLikeCount = bbsPostRepository.sumLikeCountByAuthorId(currentUserId.longValue());
        Integer commentLikeCount = bbsCommentRepository.sumLikeCountByAuthorId(currentUserId.longValue());
        overview.put("totalLikeCount", safeInteger(postLikeCount) + safeInteger(commentLikeCount));

        long totalFavoriteCount = bbsFavoriteRepository.countByUserId(currentUserId);
        overview.put("totalFavoriteCount", totalFavoriteCount);

        Integer totalViewCount = bbsPostRepository.sumViewCountByAuthorId(currentUserId.longValue());
        overview.put("totalViewCount", safeInteger(totalViewCount));

        overview.put("followingCount", safeInteger(user.getFollowingCount()));
        overview.put("followerCount", safeInteger(user.getFollowerCount()));
        return overview;
    }

    private List<Map<String, Object>> toTrendList(List<Object[]> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", row[0] != null ? row[0].toString() : "");
            item.put("count", row[1] instanceof Number ? ((Number) row[1]).longValue() : 0);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> toPostStatusDistribution(List<Object[]> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", getPostStatusName(row[0]));
            item.put("count", row[1] instanceof Number ? ((Number) row[1]).longValue() : 0);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildInteractionDistribution(Map<String, Object> overview) {
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(distributionItem("浏览", overview.get("totalViewCount")));
        result.add(distributionItem("获赞", overview.get("totalLikeCount")));
        result.add(distributionItem("被收藏", overview.get("totalFavoriteCount")));
        result.add(distributionItem("评论", overview.get("commentCount")));
        result.add(distributionItem("粉丝", overview.get("followerCount")));
        return result;
    }

    private Map<String, Object> distributionItem(String name, Object value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("count", value instanceof Number ? ((Number) value).longValue() : 0);
        return item;
    }

    private List<Map<String, Object>> buildTopPostList(List<BbsPost> posts) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (posts == null) {
            return result;
        }
        for (BbsPost post : posts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", post.getId());
            item.put("title", post.getTitle());
            item.put("viewCount", safeInteger(post.getViewCount()));
            item.put("likeCount", safeInteger(post.getLikeCount()));
            item.put("commentCount", safeInteger(post.getCommentCount()));
            item.put("favoriteCount", safeInteger(post.getFavoriteCount()));
            item.put("createTime", post.getCreateTime());
            item.put("heat", safeInteger(post.getViewCount()) + safeInteger(post.getLikeCount()) * 5
                    + safeInteger(post.getCommentCount()) * 3 + safeInteger(post.getFavoriteCount()) * 4);
            result.add(item);
        }
        return result;
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String getPostStatusName(Object statusObj) {
        if (statusObj == null) {
            return "未知";
        }
        String status = statusObj.toString();
        return switch (status) {
            case "1" -> "正常";
            case "0" -> "待审核";
            case "-1" -> "已删除";
            default -> "状态" + status;
        };
    }

    public DataResponse getMyPosts(DataRequest dataRequest) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }
        return getUserPosts(currentUserId, dataRequest);
    }

    public DataResponse getUserPosts(Integer userId, DataRequest dataRequest) {
        Integer pageNum = dataRequest.getInteger("pageNum");
        Integer pageSize = dataRequest.getInteger("pageSize");

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BbsPost> postPage = bbsPostRepository.findMyVisibleAndRejectedPosts(userId.longValue(), pageable);

        for (BbsPost post : postPage.getContent()) {
            Optional<BbsBoard> boardOptional = bbsBoardRepository.findById(post.getBoardId());
            if (boardOptional.isPresent()) {
                post.setBoardName(boardOptional.get().getName());
            }
        }

        return CommonMethod.getReturnData(postPage);
    }

    public DataResponse getMyFavorites(DataRequest dataRequest) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Integer pageNum = dataRequest.getInteger("pageNum");
        Integer pageSize = dataRequest.getInteger("pageSize");

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BbsFavorite> favoritePage = bbsFavoriteRepository.findByUserIdOrderByCreateTimeDesc(currentUserId, pageable);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (BbsFavorite favorite : favoritePage.getContent()) {
            Optional<BbsPost> postOptional = bbsPostRepository.findById(favorite.getPostId());
            if (postOptional.isPresent()) {
                BbsPost post = postOptional.get();
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("id", post.getId());
                postMap.put("title", post.getTitle());
                postMap.put("content", post.getContent());
                postMap.put("likeCount", post.getLikeCount());
                postMap.put("commentCount", post.getCommentCount());
                postMap.put("viewCount", post.getViewCount());
                postMap.put("favoriteCount", post.getFavoriteCount());
                postMap.put("createTime", post.getCreateTime());
                postMap.put("favoriteTime", favorite.getCreateTime());

                Optional<BbsBoard> boardOptional = bbsBoardRepository.findById(post.getBoardId());
                if (boardOptional.isPresent()) {
                    postMap.put("boardName", boardOptional.get().getName());
                }

                Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
                if (authorOptional.isPresent()) {
                    postMap.put("authorNickname", authorOptional.get().getNickname());
                    postMap.put("authorAvatarUrl", authorOptional.get().getAvatarUrl());
                }

                resultList.add(postMap);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", resultList);
        result.put("totalElements", favoritePage.getTotalElements());
        result.put("totalPages", favoritePage.getTotalPages());
        result.put("size", favoritePage.getSize());
        result.put("number", favoritePage.getNumber());

        return CommonMethod.getReturnData(result);
    }

    public DataResponse sendChangePasswordCode() {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<User> userOptional = userRepository.findById(currentUserId);
        if (userOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        String email = getBoundEmail(currentUserId);
        if (email == null) {
            return CommonMethod.getReturnMessageError("当前账号未绑定邮箱，请先在个人资料中绑定邮箱");
        }

        String error = emailVerificationService.sendVerificationCode(email, CHANGE_PASSWORD_VERIFICATION_TYPE);
        if (error != null) {
            return CommonMethod.getReturnMessageError(error);
        }

        return CommonMethod.getReturnMessageOK("验证码已发送至绑定邮箱，请在5分钟内完成验证");
    }

    @Transactional
    public DataResponse changePassword(DataRequest dataRequest) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<User> userOptional = userRepository.findById(currentUserId);
        if (userOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        User user = userOptional.get();
        String email = getBoundEmail(currentUserId);
        if (email == null) {
            return CommonMethod.getReturnMessageError("当前账号未绑定邮箱，请先在个人资料中绑定邮箱");
        }

        String oldPassword = dataRequest.getString("oldPassword");
        String newPassword = dataRequest.getString("newPassword");
        String emailCode = dataRequest.getString("emailCode");

        if (oldPassword == null || oldPassword.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：原密码不能为空");
        }

        if (newPassword == null || newPassword.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：新密码不能为空");
        }

        if (emailCode == null || emailCode.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：邮箱验证码不能为空");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return CommonMethod.getReturnMessageError("原密码错误");
        }

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            return CommonMethod.getReturnMessageError("参数错误：密码长度8-20，至少包含字母和数字");
        }

        if (oldPassword.equals(newPassword)) {
            return CommonMethod.getReturnMessageError("新密码不能与原密码相同");
        }

        String verifyError = emailVerificationService.verifyCode(email, emailCode, CHANGE_PASSWORD_VERIFICATION_TYPE);
        if (verifyError != null) {
            return CommonMethod.getReturnMessageError(verifyError);
        }

        userRepository.updatePassword(currentUserId, passwordEncoder.encode(newPassword));

        return CommonMethod.getReturnMessageOK("密码修改成功");
    }

    private String getBoundEmail(Integer currentUserId) {
        Optional<Person> personOptional = personRepository.findById(currentUserId);
        if (personOptional.isEmpty()) {
            return null;
        }
        String email = personOptional.get().getEmail();
        return email == null || email.isBlank() ? null : email.trim();
    }

    private boolean isMutuallyFollowing(Integer userId1, Integer userId2) {
        if (userId1 == null || userId2 == null) {
            return false;
        }
        boolean following = bbsFollowRepository.existsByFollowerIdAndFollowingId(userId1, userId2);
        boolean followed = bbsFollowRepository.existsByFollowerIdAndFollowingId(userId2, userId1);
        return following && followed;
    }

    public DataResponse getUserProfile(Integer userId) {
        Integer currentUserId = CommonMethod.getPersonId();
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        User user = userOptional.get();
        boolean isCurrentUser = currentUserId != null && currentUserId.equals(userId);
        boolean isMutuallyFollowing = isMutuallyFollowing(currentUserId, userId);

        // 只返回需要的信息，不返回敏感信息
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("userId", user.getPersonId());
        userProfile.put("studentId", user.getStudentId());
        userProfile.put("nickname", user.getNickname());
        userProfile.put("avatarUrl", user.getAvatarUrl());
        userProfile.put("signature", user.getSignature());
        userProfile.put("postCount", user.getPostCount());
        userProfile.put("followerCount", user.getFollowerCount());
        userProfile.put("followingCount", user.getFollowingCount());
        userProfile.put("isBanned", user.getIsBanned());

        // 如果是当前用户，直接返回所有 Person 信息
        if (isCurrentUser) {
            if (user.getPerson() != null) {
                Person person = user.getPerson();
                userProfile.put("personName", person.getName());
                userProfile.put("personDept", person.getDept());
                userProfile.put("personGender", person.getGender());
                userProfile.put("personBirthday", person.getBirthday());
                userProfile.put("personEmail", person.getEmail());
                userProfile.put("personPhone", person.getPhone());
                userProfile.put("personAddress", person.getAddress());
                userProfile.put("personIntroduce", person.getIntroduce());
            }
            return CommonMethod.getReturnData(userProfile);
        }

        // 如果不是当前用户，根据隐私设置过滤 Person 信息
        if (user.getPerson() != null) {
            Person person = user.getPerson();
            
            // 检查每个字段的隐私设置
            String namePrivacy = person.getNamePrivacy() != null ? person.getNamePrivacy() : "PUBLIC";
            if (namePrivacy.equals("PUBLIC") || (namePrivacy.equals("FOLLOWING") && isMutuallyFollowing)) {
                userProfile.put("personName", person.getName());
            }
            
            String deptPrivacy = person.getDeptPrivacy() != null ? person.getDeptPrivacy() : "PUBLIC";
            if (deptPrivacy.equals("PUBLIC") || (deptPrivacy.equals("FOLLOWING") && isMutuallyFollowing)) {
                userProfile.put("personDept", person.getDept());
            }
            
            String genderPrivacy = person.getGenderPrivacy() != null ? person.getGenderPrivacy() : "PUBLIC";
            if (genderPrivacy.equals("PUBLIC") || (genderPrivacy.equals("FOLLOWING") && isMutuallyFollowing)) {
                userProfile.put("personGender", person.getGender());
            }
            
            String birthdayPrivacy = person.getBirthdayPrivacy() != null ? person.getBirthdayPrivacy() : "PUBLIC";
            if (birthdayPrivacy.equals("PUBLIC") || (birthdayPrivacy.equals("FOLLOWING") && isMutuallyFollowing)) {
                userProfile.put("personBirthday", person.getBirthday());
            }
            
            String emailPrivacy = person.getEmailPrivacy() != null ? person.getEmailPrivacy() : "PUBLIC";
            if (emailPrivacy.equals("PUBLIC") || (emailPrivacy.equals("FOLLOWING") && isMutuallyFollowing)) {
                userProfile.put("personEmail", person.getEmail());
            }
            
            String phonePrivacy = person.getPhonePrivacy() != null ? person.getPhonePrivacy() : "PUBLIC";
            if (phonePrivacy.equals("PUBLIC") || (phonePrivacy.equals("FOLLOWING") && isMutuallyFollowing)) {
                userProfile.put("personPhone", person.getPhone());
            }
            
            String addressPrivacy = person.getAddressPrivacy() != null ? person.getAddressPrivacy() : "PUBLIC";
            if (addressPrivacy.equals("PUBLIC") || (addressPrivacy.equals("FOLLOWING") && isMutuallyFollowing)) {
                userProfile.put("personAddress", person.getAddress());
            }
            
            String introducePrivacy = person.getIntroducePrivacy() != null ? person.getIntroducePrivacy() : "PUBLIC";
            if (introducePrivacy.equals("PUBLIC") || (introducePrivacy.equals("FOLLOWING") && isMutuallyFollowing)) {
                userProfile.put("personIntroduce", person.getIntroduce());
            }
        }

        return CommonMethod.getReturnData(userProfile);
    }
    
    /**
     * 修复用户发帖数与实际帖子数量不一致的问题
     * 此方法会遍历所有用户，重新计算他们的有效帖子数量（status=1）
     */
    @Transactional
    public DataResponse fixPostCountInconsistency() {
        log.info("开始修复用户发帖数不一致问题...");
        
        int fixedCount = 0;
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            int currentCount = user.getPostCount() != null ? user.getPostCount() : 0;
            long actualCount = bbsPostRepository.countByAuthorIdAndStatus(user.getPersonId().longValue(), 1);
            
            if (currentCount != actualCount) {
                log.info("用户 {} (ID: {}) 发帖数不一致: 记录数={}, 实际数={}", 
                    user.getNickname(), user.getPersonId(), currentCount, actualCount);
                userRepository.updatePostCount(user.getPersonId(), (int) actualCount - currentCount);
                fixedCount++;
            }
        }
        
        log.info("修复完成，共修复了 {} 个用户的发帖数", fixedCount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", allUsers.size());
        result.put("fixedUsers", fixedCount);
        return CommonMethod.getReturnData(result);
    }
}
