package cn.edu.sdu.java.server.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public boolean sendVerificationCode(String toEmail, String code) {
        return sendVerificationCode(toEmail, code, "REGISTER");
    }

    public boolean sendVerificationCode(String toEmail, String code, String type) {
        return sendVerificationCodeWithError(toEmail, code, type) == null;
    }

    public String sendVerificationCodeWithError(String toEmail, String code, String type) {
        String configError = validateMailConfig();
        if (configError != null) {
            log.warn("邮件配置不完整：{}", configError);
            return configError;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(buildSubject(type));
            message.setText(buildContent(code, type));
            mailSender.send(message);
            log.info("邮件发送成功，收件人：{}，类型：{}", toEmail, type);
            return null;
        } catch (MailAuthenticationException e) {
            log.error("邮件发送认证失败，收件人：{}，错误：{}", toEmail, e.getMessage());
            return "邮箱授权码无效或 SMTP 服务未开启，请检查 MAIL_USERNAME 和 MAIL_PASSWORD";
        } catch (MailException e) {
            log.error("邮件发送失败，收件人：{}，错误：{}", toEmail, e.getMessage());
            return "邮件发送失败，请稍后重试";
        }
    }

    private String validateMailConfig() {
        if (fromEmail == null || fromEmail.isBlank()) {
            return "未配置发件邮箱，请设置环境变量 MAIL_USERNAME";
        }
        if (mailPassword == null || mailPassword.isBlank()) {
            return "未配置邮箱授权码，请设置环境变量 MAIL_PASSWORD";
        }
        return null;
    }

    private String buildSubject(String type) {
        if ("CHANGE_PASSWORD".equals(type)) {
            return "【校园论坛】修改密码验证码";
        }
        return "【校园论坛】注册验证码";
    }

    private String buildContent(String code, String type) {
        String action = "CHANGE_PASSWORD".equals(type) ? "修改密码" : "注册";
        return "亲爱的用户：\n\n"
                + "您的" + action + "验证码是：" + code + "\n\n"
                + "该验证码5分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您本人的操作，请忽略此邮件。\n\n"
                + "此致\n"
                + "校园论坛团队";
    }
}
