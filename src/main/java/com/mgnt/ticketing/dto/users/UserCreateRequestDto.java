package com.mgnt.ticketing.dto.users;

public record UserCreateRequestDto(
        String email,
        String password,
        String name
) {}