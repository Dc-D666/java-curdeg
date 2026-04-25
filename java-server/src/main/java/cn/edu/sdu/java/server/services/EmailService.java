package cn.edu.sdu.java.server.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("【校园论坛】注册验证码");
            message.setText("亲爱的用户：\n\n" +
                    "您的注册验证码是：" + code + "\n\n" +
                    "该验证码5分钟内有效，请勿泄露给他人。\n\n" +
                    "如果这不是您本人的操作，请忽略此邮件。\n\n" +
                    "此致\n" +
                    "校园论坛团队");
            mailSender.send(message);
            log.info("邮件发送成功，收件人：{}", toEmail);
            return true;
        } catch (MailException e) {
            log.error("邮件发送失败，收件人：{}，错误：{}", toEmail, e.getMessage());
            return false;
        }
    }
}
