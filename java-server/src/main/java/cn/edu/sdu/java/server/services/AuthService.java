package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.request.LoginRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.JwtResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import cn.edu.sdu.java.server.util.LoginControlUtil;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final StudentRepository studentRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final EmailVerificationService verificationService;
    private final PointService pointService;
    private final EntityManager entityManager;

    public AuthService(PersonRepository personRepository, UserRepository userRepository, UserTypeRepository userTypeRepository, StudentRepository studentRepository, AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder encoder, ResourceLoader resourceLoader, EmailVerificationService verificationService, PointService pointService, EntityManager entityManager) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.studentRepository = studentRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.encoder = encoder;
        this.verificationService = verificationService;
        this.pointService = pointService;
        this.entityManager = entityManager;
    }
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Optional<User> op= userRepository.findByUserName(loginRequest.getUsername());
        if(op.isPresent()) {
            User user= op.get();
            String lastLoginTime = DateTimeTool.parseDateTime(new Date());
            Integer count = user.getLoginCount();
            if (count == null)
                count = 1;
            else count += 1;

            Integer userId = user.getPersonId();
            Integer consecutiveDays = user.getConsecutiveLoginDays();

            // 登录积分逻辑
            LocalDate today = LocalDate.now();
            LocalDate lastLoginDate = user.getLastLoginDate();
            if (lastLoginDate == null || !lastLoginDate.equals(today)) {
                pointService.addPoints(userId, "DAILY_LOGIN", "每日登录", null, null);

                // 更新连续登录天数
                if (lastLoginDate != null && lastLoginDate.plusDays(1).equals(today)) {
                    consecutiveDays = (consecutiveDays != null ? consecutiveDays : 0) + 1;
                } else {
                    consecutiveDays = 1;
                }

                // 连续登录里程碑奖励
                if (consecutiveDays == 2) {
                    pointService.addPoints(userId, "CONSECUTIVE_LOGIN_2", "连续登录2天", null, null);
                } else if (consecutiveDays == 3) {
                    pointService.addPoints(userId, "CONSECUTIVE_LOGIN_3", "连续登录3天", null, null);
                } else if (consecutiveDays == 7) {
                    pointService.addPoints(userId, "CONSECUTIVE_LOGIN_7", "连续登录7天", null, null);
                } else if (consecutiveDays == 15) {
                    pointService.addPoints(userId, "CONSECUTIVE_LOGIN_15", "连续登录15天", null, null);
                } else if (consecutiveDays == 30) {
                    pointService.addPoints(userId, "CONSECUTIVE_LOGIN_30", "连续登录30天", null, null);
                } else if (consecutiveDays > 30 && consecutiveDays % 7 == 0) {
                    int weeks = consecutiveDays / 7;
                    pointService.addPoints(userId, "CONSECUTIVE_LOGIN_WEEKLY",
                            "连续登录" + consecutiveDays + "天(第" + weeks + "周)", null, null);
                }
            }

            String lastLoginDateStr = today.toString();
            userRepository.updateLoginInfo(userId, lastLoginTime, count, consecutiveDays, lastLoginDateStr);
        }
        String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getPerName(),
                roles.getFirst()));
    }
    public DataResponse getValidateCode(DataRequest dataRequest) {
        return CommonMethod.getReturnData(LoginControlUtil.getInstance().getValidateCodeDataMap());
    }

    public DataResponse testValidateInfo( DataRequest dataRequest) {
        Integer validateCodeId = dataRequest.getInteger("validateCodeId");
        String validateCode = dataRequest.getString("validateCode");
        LoginControlUtil li =  LoginControlUtil.getInstance();
        if(validateCodeId == null || validateCode== null || validateCode.isEmpty()) {
            return CommonMethod.getReturnMessageError("验证码为空！");
        }
        String value = li.getValidateCode(validateCodeId);
        if(!validateCode.equals(value))
            return CommonMethod.getReturnMessageError("验证码错位！");
        return CommonMethod.getReturnMessageOK();
    }
    /*
     *  注册用户示例，我们项目暂时不用， 所有用户通过管理员添加，这里注册，没有考虑关联人员信息的创建，使用时参加学生添加功能的实现
     */
    @Transactional
    @PostMapping("/registerUser")
    public DataResponse registerUser(@Valid @RequestBody DataRequest dataRequest) {
        String username = dataRequest.getString("username");
        String password = dataRequest.getString("password");
        String perName = dataRequest.getString("perName");
        String email = dataRequest.getString("email");
        String emailCode = dataRequest.getString("emailCode");

        if (emailCode == null || emailCode.isEmpty()) {
            return CommonMethod.getReturnMessageError("请输入邮箱验证码！");
        }

        String verifyError = verificationService.verifyCode(email, emailCode, "REGISTER");
        if (verifyError != null) {
            return CommonMethod.getReturnMessageError(verifyError);
        }

        Optional<User> uOp = userRepository.findByUserName(username);
        if(uOp.isPresent()) {
            return CommonMethod.getReturnMessageError("用户已经存在，不能注册！");
        }
        Person p = new Person();
        p.setNum(username);
        p.setName(perName);
        p.setEmail(email);
        p.setType("2");
        UserType ut = userTypeRepository.findByName(EUserType.ROLE_STUDENT.name());
        personRepository.saveAndFlush(p);  // 保存Person后，p.getPersonId()会有值
        User u = new User();
        u.setPersonId(p.getPersonId());  // 必须手动设置主键！
        u.setUserType(ut);
        u.setUserName(username);
        u.setPassword(encoder.encode(password));
        u.setCreateTime(DateTimeTool.parseDateTime(new Date()));
        u.setCreatorId(p.getPersonId());
        u.setLoginCount(0);
        u.setStudentId(username);
        u.setNickname(perName);
        entityManager.persist(u);
        entityManager.flush();
        return CommonMethod.getReturnMessageOK("注册成功！");
    }

    public DataResponse resetPassword(String studentId, String email, String emailCode, String newPassword) {
        if (studentId == null || studentId.isEmpty()) {
            return CommonMethod.getReturnMessageError("学号不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return CommonMethod.getReturnMessageError("密码长度不能小于6位");
        }
        String verifyError = verificationService.verifyCode(email, emailCode, "RESET_PASSWORD");
        if (verifyError != null) {
            return CommonMethod.getReturnMessageError(verifyError);
        }
        Optional<User> uOp = userRepository.findByUserName(studentId);
        if (uOp.isEmpty()) {
            return CommonMethod.getReturnMessageError("该学号未注册");
        }
        User user = uOp.get();
        if (user.getPerson() == null || user.getPerson().getEmail() == null || !user.getPerson().getEmail().equals(email)) {
            return CommonMethod.getReturnMessageError("学号与邮箱不匹配");
        }
        userRepository.updatePassword(user.getPersonId(), encoder.encode(newPassword));
        return CommonMethod.getReturnMessageOK("密码重置成功");
    }

}
