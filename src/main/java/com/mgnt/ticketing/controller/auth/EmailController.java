package com.mgnt.ticketing.controller.auth;

import com.mgnt.ticketing.controller.auth.response.EmailResponseDto;
import com.mgnt.ticketing.domain.auth.service.EmailInterface;
import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이메일", description = "Email Controller")
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailInterface emailInterface;
    private final UserJpaRepository userJpaRepository;

    @Operation(summary = "이메일 인증", description = "이메일 인증을 위한 API입니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EmailResponseDto.class)))
    @GetMapping
    public ResponseEntity<? super EmailResponseDto> verifyEmail(@RequestParam(value = "token") String token) {
        return emailInterface.verifyEmail(token);
    }
}
