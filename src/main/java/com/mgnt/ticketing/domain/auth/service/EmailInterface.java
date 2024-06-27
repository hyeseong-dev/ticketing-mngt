package com.mgnt.ticketing.domain.auth.service;

import com.mgnt.ticketing.controller.auth.request.EmailRequestDto;
import com.mgnt.ticketing.controller.auth.response.EmailResponseDto;
import org.springframework.http.ResponseEntity;

public interface EmailInterface {

    void sendEmail(EmailRequestDto emailMessage);
    ResponseEntity<? super EmailResponseDto> verifyEmail(String token);
    void sendVerificationEmail(String email, String name);
}
