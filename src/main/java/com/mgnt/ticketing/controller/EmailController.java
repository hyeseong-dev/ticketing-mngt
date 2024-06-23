package com.mgnt.ticketing.controller;

import com.mgnt.ticketing.dto.response.ResponseMessage;
import com.mgnt.ticketing.dto.response.auth.EmailResponseDto;
import com.mgnt.ticketing.repository.UserRepository;
import com.mgnt.ticketing.service.EmailService;
import com.mgnt.ticketing.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<? super EmailResponseDto> verifyEmail(@RequestParam(value="token") String token) {
        return emailService.verifyEmail(token);

    }
}