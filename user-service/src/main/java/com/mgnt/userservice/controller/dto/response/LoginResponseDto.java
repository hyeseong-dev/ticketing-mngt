package com.mgnt.userservice.controller.dto.response;

public record LoginResponseDto(
        Long userId,
        String email,
        String name,
        String role,
        String accessToken
) {
}