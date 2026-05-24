package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.request.LoginRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.services.AuthService;
import cn.edu.sdu.java.server.services.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


/**
 *  AuthController 实现 登录和注册Web服务
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService verificationService;

    public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder, EmailVerificationService verificationService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
    }

    /**
     *  用户登录
     * @param loginRequest   username 登录名  password 密码
     * @return   JwtResponse 用户信息， 该信息再后续的web请求时作为请求头的一部分，用于框架的请求服务权限验证
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }
    @PostMapping("/getValidateCode")
    public DataResponse getValidateCode(@Valid @RequestBody DataRequest dataRequest) {
        return authService.getValidateCode(dataRequest);
    }

    @PostMapping("/testValidateInfo")
    public DataResponse testValidateInfo(@Valid @RequestBody DataRequest dataRequest) {
        return authService.testValidateInfo(dataRequest);
    }

    @PostMapping("/sendEmailCode")
    public DataResponse sendEmailCode(@Valid @RequestBody DataRequest dataRequest) {
        String email = dataRequest.getString("email");
        String type = dataRequest.getString("type");
        if (type == null || type.isEmpty()) {
            type = "REGISTER";
        }
        String error = verificationService.sendVerificationCode(email, type);
        if (error != null) {
            return cn.edu.sdu.java.server.util.CommonMethod.getReturnMessageError(error);
        }
        return cn.edu.sdu.java.server.util.CommonMethod.getReturnMessageOK("验证码已发送，请查收邮件");
    }

    @PostMapping("/registerUser")
    public DataResponse registerUser(@Valid @RequestBody DataRequest dataRequest) {
        return authService.registerUser(dataRequest);
    }

    @PostMapping("/resetAdminPassword")
    public DataResponse resetAdminPassword() {
        try {
            User adminUser = userRepository.findByUserName("admin").orElse(null);
            if (adminUser == null) {
                return cn.edu.sdu.java.server.util.CommonMethod.getReturnMessageError("Admin user not found!");
            }
            adminUser.setPassword(passwordEncoder.encode("123456"));
            userRepository.updatePassword(adminUser.getPersonId(), passwordEncoder.encode("123456"));
            return cn.edu.sdu.java.server.util.CommonMethod.getReturnMessageOK("Admin password reset successfully to '123456'");
        } catch (Exception e) {
            return cn.edu.sdu.java.server.util.CommonMethod.getReturnMessageError("Error resetting password: " + e.getMessage());
        }
    }

    @PostMapping("/resetPassword")
    public DataResponse resetPassword(@Valid @RequestBody DataRequest dataRequest) {
        String studentId = dataRequest.getString("studentId");
        String email = dataRequest.getString("email");
        String emailCode = dataRequest.getString("emailCode");
        String newPassword = dataRequest.getString("newPassword");
        return authService.resetPassword(studentId, email, emailCode, newPassword);
    }
}
