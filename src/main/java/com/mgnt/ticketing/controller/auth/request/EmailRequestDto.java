package com.mgnt.ticketing.controller.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {

    @NotBlank
    @Email(message = "이메일 형식이 아닙니다.")
    private String to;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;
}