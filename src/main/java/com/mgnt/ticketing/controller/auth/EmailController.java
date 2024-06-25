package com.mgnt.ticketing.controller.auth;

import com.mgnt.ticketing.controller.auth.dto.response.EmailResponseDto;
import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import com.mgnt.ticketing.domain.auth.service.EmailInterface;
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

    private final EmailInterface emailInterface;
    private final UserJpaRepository userJpaRepository;

    @GetMapping
    public ResponseEntity<? super EmailResponseDto> verifyEmail(@RequestParam(value="token") String token) {
        return emailInterface.verifyEmail(token);

    }
}