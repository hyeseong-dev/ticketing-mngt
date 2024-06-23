package com.mgnt.ticketing.service.implement;

import com.mgnt.ticketing.common.error.ErrorCode;
import com.mgnt.ticketing.dto.request.auth.EmailRequestDto;
import com.mgnt.ticketing.dto.response.auth.EmailResponseDto;
import com.mgnt.ticketing.entity.UserEntity;
import com.mgnt.ticketing.repository.UserRepository;
import com.mgnt.ticketing.service.EmailService;
import com.mgnt.ticketing.util.EncryptionUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailServiceImplement implements EmailService {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ResponseEntity<? super EmailResponseDto> verifyEmail(String token) {
        try {
            String email = EncryptionUtil.decrypt(token);
            UserEntity user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                if(!user.getEmailVerified()){
                    user.setEmailVerified(true);
                    userRepository.save(user);
                }
                return ResponseEntity.ok(EmailResponseDto.success());
            } else {
                return ResponseEntity.badRequest().body(EmailResponseDto.failure(ErrorCode.TOKEN_INVALID));
            }
        } catch (Exception e) {
            log.error("오류 : " + e.getMessage());
            return ResponseEntity.badRequest().body(EmailResponseDto.failure(ErrorCode.TOKEN_INVALID));
        }
    }

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
            String verificationLink = "http://localhost:8080/api/email?token=" + encryptedEmail;
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