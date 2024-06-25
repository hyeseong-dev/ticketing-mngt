package com.mgnt.ticketing.service;

import com.mgnt.ticketing.dto.request.user.AdminModifyRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyMypageRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyPasswordRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyRequestDto;
import com.mgnt.ticketing.dto.response.user.*;
import com.mgnt.ticketing.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UserService {
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
