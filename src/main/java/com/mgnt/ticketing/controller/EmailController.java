package com.mgnt.ticketing.controller;

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
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("이메일 인증에 실패했습니다.");
        }
    }
}