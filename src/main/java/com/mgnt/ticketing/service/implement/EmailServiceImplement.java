package com.mgnt.ticketing.service.implement;

import com.mgnt.ticketing.dto.request.auth.EmailRequestDto;
import com.mgnt.ticketing.service.EmailService;
import com.mgnt.ticketing.util.EncryptionUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailServiceImplement implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailRequestDto emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getBody(), true);
            javaMailSender.send(mimeMessage);
            log.info("sent email: {}", emailMessage.getBody());
        }catch (MessagingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendVerificationEmail(String email, String name) {
        try {
            String encryptedEmail = EncryptionUtil.encrypt(email);
            String verificationLink = "http://localhost:8080/api/email/token?token=" + encryptedEmail;
            String body = "안녕하세요, " + name + "님!\n\n" +
                    "다음 링크를 클릭하여 이메일 인증을 완료해주세요:\n" +
                    verificationLink;

            EmailRequestDto emailRequestDto = new EmailRequestDto(email, "이메일 인증", body);
            sendEmail(emailRequestDto);
        } catch (Exception e) {
            log.error("이메일 발송 중 오류 발생: {}", e.getMessage());
        }
    }

}