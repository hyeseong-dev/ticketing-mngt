package com.mgnt.userservice.controller.dto.response;

public record RefreshTokenResponseDto(
        Long userId,
        String email,
        String name,
        String role,
        String accessToken,
        String refreshToken
) {
}