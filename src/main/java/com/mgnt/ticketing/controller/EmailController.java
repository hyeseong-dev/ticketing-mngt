package com.mgnt.ticketing.controller;

import com.mgnt.ticketing.dto.response.ResponseMessage;
import com.mgnt.ticketing.repository.UserRepository;
import com.mgnt.ticketing.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final UserRepository userRepository;

    @GetMapping("/api/email/token")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            String email = EncryptionUtil.decrypt(token);
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setEmailVerified(true);
                userRepository.save(user);
            });
            return ResponseEntity.ok(ResponseMessage.EMAIL_VERIFICATION_SUCCESS);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseMessage.EMAIL_VERIFICATION_FAILED);
        }
    }
}