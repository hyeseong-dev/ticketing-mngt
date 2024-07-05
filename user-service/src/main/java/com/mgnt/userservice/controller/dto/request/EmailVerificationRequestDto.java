package com.mgnt.userservice.controller.dto.request;

public record EmailVerificationRequestDto(String email, String code) {
}