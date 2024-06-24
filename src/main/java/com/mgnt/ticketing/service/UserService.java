package com.mgnt.ticketing.service;

import com.mgnt.ticketing.dto.request.user.AdminModifyRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyMypageRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyRequestDto;
import com.mgnt.ticketing.dto.response.user.AdminModifyResponseDto;
import com.mgnt.ticketing.dto.response.user.UserDeleteResponseDto;
import com.mgnt.ticketing.dto.response.user.UserDetailResponseDto;
import com.mgnt.ticketing.dto.response.user.UserListResponseDto;
import com.mgnt.ticketing.dto.response.user.UserModifyResponseDto;
import com.mgnt.ticketing.entity.UserEntity;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserService {
    ResponseEntity<UserListResponseDto> getUsers();
    ResponseEntity<UserDetailResponseDto> getUserDetail(Long id);

    ResponseEntity<AdminModifyResponseDto> modifyUserByAdmin(Long id, AdminModifyRequestDto requestBody);
    ResponseEntity<UserDeleteResponseDto> deleteUser(Long id);

    Optional<UserEntity> getUserByEmail(String currentUserEmail);
    Optional<UserEntity> getUserById(Long resourceId);

    ResponseEntity<UserModifyResponseDto> modifyUser(Long id, @Valid UserModifyRequestDto requestBody);

    ResponseEntity<UserModifyResponseDto> modifyMypage(Long id, @Valid UserModifyMypageRequestDto requestBody);
}
