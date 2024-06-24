package com.mgnt.ticketing.service;

import com.mgnt.ticketing.dto.request.user.AdminModifyRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyRequestDto;
import com.mgnt.ticketing.dto.response.user.AdminModifyResponseDto;
import com.mgnt.ticketing.dto.response.user.UserDeleteResponseDto;
import com.mgnt.ticketing.dto.response.user.UserDetailResponseDto;
import com.mgnt.ticketing.dto.response.user.UserListResponseDto;
import com.mgnt.ticketing.dto.response.user.UserModifyResponseDto;
import com.mgnt.ticketing.entity.UserEntity;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UserService {
    ResponseEntity<UserListResponseDto> getUsers();
    ResponseEntity<UserDetailResponseDto> getUserDetail(Long id);
    ResponseEntity<UserModifyResponseDto> modifyUser(Long id, @Valid UserModifyRequestDto requestBody);
    ResponseEntity<AdminModifyResponseDto> modifyUserByAdmin(Long id, AdminModifyRequestDto requestBody);
    ResponseEntity<UserDeleteResponseDto> deleteUser(Long id);

    Optional<UserEntity> getUserByEmail(String currentUserEmail);
    Optional<UserEntity> getUserById(Long resourceId);
}
