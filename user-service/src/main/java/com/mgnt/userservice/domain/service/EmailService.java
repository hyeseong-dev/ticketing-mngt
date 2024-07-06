package com.mgnt.userservice.domain.service;

import com.mgnt.core.exception.CustomException;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.userservice.domain.entity.Users;
import com.mgnt.userservice.domain.repository.UserRepository;
import com.mgnt.userservice.utils.EmailValidator;
import com.mgnt.userservice.utils.RedisUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService implements EmailInterface {

    @Value("${spring.mail.username}")
    private String sender;

    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final RedisUtils redisUtils;

    @Override
    public void sendVerificationEmail(String toEmail, String name) throws MessagingException, UnsupportedEncodingException {
        boolean validate = EmailValidator.validate(toEmail);
        if (!validate) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_FORMAT, null, Level.WARN);
        }

        if (redisUtils.existKey(toEmail)) {
            redisUtils.deleteKey(toEmail);
        }
        MimeMessage emailForm = createEmailForm(toEmail, name);
        javaMailSender.send(emailForm);
    }

    private MimeMessage createEmailForm(String email, String name) throws MessagingException, UnsupportedEncodingException {
        String authCode = createVerificationCode();
        MimeMessage message = javaMailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("Ticketing Service: Email Verification Code");

        String msg = String.format(
                "<h1>Email Verification</h1>" +
                        "<p>Hello %s,</p>" +
                        "<p>Your verification code is:</p>" +
                        "<h2>%s</h2>" +
                        "<p>Please enter this code in the signup screen to complete your registration.</p>",
                name, authCode
        );

        message.setText(msg, "utf-8", "html");
        message.setFrom(new InternetAddress(sender, "Ticketing Admin"));

        redisUtils.setData(email, authCode, 60 * 5L); // 5 minutes expiration
        return message;
    }

    private String createVerificationCode() {
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(rnd.nextInt(10));
        }
        return code.toString();
    }

    @Override
    public boolean verifyEmail(String email, String code) {
        String storedCode = redisUtils.getCode(email);
        if (storedCode == null) {
            throw new CustomException(ErrorCode.VERIFICATION_CODE_NOT_FOUND, null, Level.WARN);
        }
        boolean isValid = storedCode.equals(code);
        if (isValid) {
            redisUtils.deleteKey(email);
        }
        Users user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, null, Level.WARN));

        user.setEmailVerified(true);
        userRepository.save(user);
        return isValid;
    }
}