package com.mgnt.ticketing.domain.user.service;

import com.mgnt.ticketing.controller.user.dto.request.AdminModifyRequestDto;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyMypageRequestDto;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyPasswordRequestDto;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyRequestDto;
import com.mgnt.ticketing.controller.user.dto.response.*;
import com.mgnt.ticketing.domain.user.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UserInterface {
    ResponseEntity<UserListResponseDto> getUsers();
    ResponseEntity<UserDetailResponseDto> getUserDetail(Long id);

    ResponseEntity<AdminModifyResponseDto> modifyUserByAdmin(Long id, AdminModifyRequestDto requestBody);
    ResponseEntity<UserDeleteResponseDto> deleteUser(Long id);

    Optional<User> getUserByEmail(String currentUserEmail);
    Optional<User> getUserById(Long resourceId);

    ResponseEntity<UserModifyResponseDto> modifyUser(Long id, UserModifyRequestDto requestBody);

    ResponseEntity<UserModifyResponseDto> modifyMypage(Long id, UserModifyMypageRequestDto requestBody);
    ResponseEntity<UserModifyResponseDto> modifyUserPassword(Long id, UserModifyPasswordRequestDto requestBody);
}
