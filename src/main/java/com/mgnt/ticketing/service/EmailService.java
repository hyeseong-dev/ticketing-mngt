package com.mgnt.ticketing.service;

import com.mgnt.ticketing.dto.request.auth.EmailRequestDto;
import com.mgnt.ticketing.dto.response.auth.EmailResponseDto;
import org.springframework.http.ResponseEntity;

public interface EmailService {

    void sendEmail(EmailRequestDto emailMessage);
    ResponseEntity<? super EmailResponseDto> verifyEmail(String token);
    void sendVerificationEmail(String email, String name);
}
