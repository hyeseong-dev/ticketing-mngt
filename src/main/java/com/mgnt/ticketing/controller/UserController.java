package com.mgnt.ticketing.controller;

import com.mgnt.ticketing.dto.request.user.UserModifyRequestDto;
import com.mgnt.ticketing.dto.response.user.UserDeleteResponseDto;
import com.mgnt.ticketing.dto.response.user.UserDetailResponseDto;
import com.mgnt.ticketing.dto.response.user.UserListResponseDto;
import com.mgnt.ticketing.dto.response.user.UserModifyResponseDto;
import com.mgnt.ticketing.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserListResponseDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponseDto> getUserDetail(@PathVariable Long id) {
        return userService.getUserDetail(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserModifyResponseDto> modifyUser(
            @PathVariable Long id,
            @RequestBody @Valid UserModifyRequestDto requestBody)
    {
        return userService.modifyUser(id, requestBody);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<UserDeleteResponseDto> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}
