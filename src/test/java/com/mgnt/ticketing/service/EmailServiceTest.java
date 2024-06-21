package com.mgnt.ticketing.service;

import com.mgnt.ticketing.dto.request.auth.EmailRequestDto;
import com.mgnt.ticketing.service.implement.EmailServiceImplement;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("이메일 전송 테스트 - 성공")
    void givenEmailRequestDto_WhenSendEmail_ThenEmailIsSent() throws MessagingException {
        // Given
        EmailRequestDto emailRequestDto = new EmailRequestDto("test@example.com", "Subject", "Body");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendEmail(emailRequestDto);

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("이메일 인증 전송 테스트 - 성공")
    void givenUserDetails_WhenSendVerificationEmail_ThenVerificationEmailIsSent() throws Exception {
        // Given
        String email = "test@example.com";
        String name = "Test User";
        String encryptedEmail = "encryptedEmail";
        String verificationLink = "http://localhost:8080/api/email/token?token=" + encryptedEmail;

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendVerificationEmail(email, name);

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage);
    }
}
