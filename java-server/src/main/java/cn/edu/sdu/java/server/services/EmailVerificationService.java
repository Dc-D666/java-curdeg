package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.EmailVerification;
import cn.edu.sdu.java.server.repositorys.EmailVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class EmailVerificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;
    private static final int MAX_TRIES = 5;
    private static final int MAX_DAILY_SENDS = 10;

    @Autowired
    private EmailVerificationRepository verificationRepository;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public String sendVerificationCode(String email, String type) {
        Optional<EmailVerification> existingOp = verificationRepository.findByEmailAndVerificationType(email, type);

        if (existingOp.isPresent()) {
            EmailVerification existing = existingOp.get();
            long secondsSinceLastSend = (System.currentTimeMillis() - existing.getUpdateTime().getTime()) / 1000;

            if (secondsSinceLastSend < SEND_INTERVAL_SECONDS) {
                return "发送太频繁，请稍后再试";
            }

            Long todayCount = verificationRepository.countTodaySend(email, type);
            if (todayCount >= MAX_DAILY_SENDS) {
                return "今日发送次数已达上限，请明日再试";
            }
        }

        String code = generateCode();
        String sendError = emailService.sendVerificationCodeWithError(email, code, type);
        if (sendError != null) {
            log.warn("验证码邮件发送失败，未保存验证码，邮箱：{}，类型：{}，原因：{}", email, type, sendError);
            return sendError;
        }

        EmailVerification verification;
        if (existingOp.isPresent()) {
            verification = existingOp.get();
            verification.setVerificationCode(code);
            verification.setSendCount(verification.getSendCount() + 1);
            verification.setVerifyCount(0);
            verification.setIsVerified(false);
            verification.setExpireTime(calculateExpireTime());
        } else {
            verification = new EmailVerification();
            verification.setEmail(email);
            verification.setVerificationCode(code);
            verification.setVerificationType(type);
            verification.setExpireTime(calculateExpireTime());
        }

        verificationRepository.save(verification);
        return null;
    }

    public String verifyCode(String email, String code, String type) {
        Optional<EmailVerification> verificationOp = verificationRepository.findValidVerification(email, type, new Date());

        if (verificationOp.isEmpty()) {
            return "验证码已过期，请重新获取";
        }

        EmailVerification verification = verificationOp.get();

        if (verification.getIsVerified()) {
            return "验证码已使用，请重新获取";
        }

        if (verification.getVerifyCount() >= MAX_TRIES) {
            return "验证次数过多，请重新获取验证码";
        }

        verification.setVerifyCount(verification.getVerifyCount() + 1);

        if (!verification.getVerificationCode().equals(code)) {
            verificationRepository.save(verification);
            return "验证码错误，请重新输入";
        }

        verification.setIsVerified(true);
        verificationRepository.save(verification);
        return null;
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    private Date calculateExpireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, CODE_EXPIRE_MINUTES);
        return calendar.getTime();
    }
}
