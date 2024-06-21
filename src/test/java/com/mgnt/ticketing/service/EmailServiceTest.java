package com.mgnt.ticketing.service;

import com.mgnt.ticketing.dto.request.auth.EmailRequestDto;
import com.mgnt.ticketing.service.implement.EmailServiceImplement;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailServiceImplement emailService;

    @Test
    void testSendEmail() throws MessagingException {
        // Given
        EmailRequestDto emailRequestDto = new EmailRequestDto("test@example.com", "Subject", "Body");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

        // When
        emailService.sendEmail(emailRequestDto);

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendVerificationEmail() throws Exception {
        // Given
        String email = "test@example.com";
        String name = "Test User";
        String encryptedEmail = "encryptedEmail";
        String verificationLink = "http://localhost:8080/api/email/token?token=" + encryptedEmail;

        EmailRequestDto emailRequestDto = new EmailRequestDto(email, "이메일 인증", "안녕하세요, Test User님!\n\n다음 링크를 클릭하여 이메일 인증을 완료해주세요:\n" + verificationLink);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

        // When
        emailService.sendVerificationEmail(email, name);

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage);
    }
}
