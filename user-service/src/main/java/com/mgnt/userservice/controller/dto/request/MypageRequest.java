package com.mgnt.userservice.controller.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MypageRequest {

    @NotNull
    @NotBlank
    @Pattern(regexp = "^(010-\\d{4}-\\d{4}|010-\\d{3}-\\d{4})$", message = "전화번호는 '010-5897-4859' 또는 '010-123-1234' 형식이어야 합니다.")
    private String phoneNumber;

    @NotNull
    @NotBlank
    @Size(min = 3, max = 100, message = "주소는 세 글자 이상 백자 이하이어야 합니다.")
    @Pattern(regexp = "^[가-힣\\d\\s]+$", message = "주소는 한글로 세 글자 이상 백자 이하 이어야 합니다.")
    private String address;
}