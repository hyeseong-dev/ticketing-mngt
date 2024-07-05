package com.mgnt.userservice.controller.dto.request;

public record EmailRequestDto(
        String to,
        String subject,
        String body
) {

}
