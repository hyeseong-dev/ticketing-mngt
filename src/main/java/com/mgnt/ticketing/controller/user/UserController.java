package com.mgnt.ticketing.controller.user;

import com.mgnt.ticketing.controller.user.dto.request.UserModifyMypageRequestDto;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyPasswordRequestDto;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyRequestDto;
import com.mgnt.ticketing.controller.user.dto.response.UserDeleteResponseDto;
import com.mgnt.ticketing.controller.user.dto.response.UserDetailResponseDto;
import com.mgnt.ticketing.controller.user.dto.response.UserListResponseDto;
import com.mgnt.ticketing.controller.user.dto.response.UserModifyResponseDto;
import com.mgnt.ticketing.domain.user.service.UserInterface;
import com.mgnt.ticketing.base.util.aop.CheckAccess;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "User 관련 API 입니다.")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserInterface userInterface;
    private final String resourceType = "USER";

    @CheckAccess(roles = {"ADMIN"}, resourceType = resourceType)
    @GetMapping
    public ResponseEntity<UserListResponseDto> getUsers() {
        return userInterface.getUsers();
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponseDto> getUserDetail(@PathVariable Long id) {
        return userInterface.getUserDetail(id);
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @PatchMapping("/{id}")
    public ResponseEntity<UserModifyResponseDto> modifyUser(
            @PathVariable Long id,
            @RequestBody @Valid UserModifyRequestDto requestBody
    ) {
        return userInterface.modifyUser(id, requestBody);
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @PatchMapping("/mypage/{id}")
    public ResponseEntity<UserModifyResponseDto> modifyMypage(
            @PathVariable Long id,
            @RequestBody @Valid UserModifyMypageRequestDto requestBody
    ) {
        return userInterface.modifyMypage(id, requestBody);
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @PatchMapping("/{id}/password")
    public ResponseEntity<UserModifyResponseDto> modifyPassword(
            @PathVariable Long id,
            @RequestBody @Valid UserModifyPasswordRequestDto requestBody
    ) {
        return userInterface.modifyUserPassword(id, requestBody);
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @DeleteMapping("/{id}")
    public ResponseEntity<UserDeleteResponseDto> deleteUser(
            @PathVariable Long id
    ) {
        return userInterface.deleteUser(id);
    }

}
