package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.EUserType;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.models.UserType;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.repositorys.UserTypeRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class BbsUserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public BbsUserService(UserRepository userRepository, PersonRepository personRepository,
                          UserTypeRepository userTypeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.userTypeRepository = userTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{6,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^\\d{10,20}$");

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

        if (user.getUserType() != null) {
            user.setAuthority(user.getUserType().getName());
        }

        return CommonMethod.getReturnData(user);
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

        userRepository.saveAndFlush(user);

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
}
