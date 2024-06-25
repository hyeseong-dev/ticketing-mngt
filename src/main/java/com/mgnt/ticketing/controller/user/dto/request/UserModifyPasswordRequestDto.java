package com.mgnt.ticketing.controller.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserModifyPasswordRequestDto {

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 여덟자 이상 스무자 이하여야 합니다.")
    private String newPassword1;

    @NotBlank(message = "새 비밀번호를 다시 입력해주세요.")
    private String newPassword2;
}
