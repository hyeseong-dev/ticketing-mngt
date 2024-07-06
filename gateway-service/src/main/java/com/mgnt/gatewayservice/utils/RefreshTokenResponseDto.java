package com.mgnt.gatewayservice.utils;

public record RefreshTokenResponseDto(
        Long userId,
        String email,
        String name,
        String role,
        String accessToken,
        String refreshToken
) {
    public static RefreshTokenResponseDto success(Long userId, String email, String name, String role, String accessToken, String refreshToken) {
        return new RefreshTokenResponseDto(userId, email, name, role, accessToken, refreshToken);
    }

    public static RefreshTokenResponseDto failure() {
        return new RefreshTokenResponseDto(null, null, null, null, null, null);
    }
}
