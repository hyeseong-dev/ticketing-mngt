package com.mgnt.ticketing.domain.user.service;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.controller.user.dto.request.AdminModifyRequestDto;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyMypageRequestDto;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyPasswordRequestDto;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyRequestDto;
import com.mgnt.ticketing.controller.user.dto.response.*;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserInterface {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ResponseEntity<UserModifyResponseDto> modifyUserPassword(Long id, UserModifyPasswordRequestDto requestBody) {
        User user = userJpaRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(requestBody.getCurrentPassword(), user.getPassword())) {
            return UserModifyResponseDto.failure(ErrorCode.CURRENT_PASSWORD_NOT_MATCHED);
        }

        if (!requestBody.getNewPassword1().equals(requestBody.getNewPassword2())) {
            return UserModifyResponseDto.failure(ErrorCode.NEW_PASSWORD_NOT_MATCHED);
        }

        user.updatePassword(passwordEncoder.encode(requestBody.getNewPassword1()));
        userJpaRepository.save(user);
        return UserModifyResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    public ResponseEntity<UserListResponseDto> getUsers() {
        List<UserResponseDto> users = userJpaRepository.findAllByDeletedAtNull()
                .stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
        return UserListResponseDto.success(users);
    }

    @Override
    public ResponseEntity<UserDetailResponseDto> getUserDetail(Long id) {
        User user = userJpaRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDetailResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UserModifyResponseDto> modifyUser(Long id, @Valid UserModifyRequestDto requestBody) {
        User user = userJpaRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 이메일 중복 확인
        if (!user.getEmail().equals(requestBody.getEmail()) && userJpaRepository.existsByEmail(requestBody.getEmail())) {
            return UserModifyResponseDto.failure(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        user.updateEmail(requestBody.getEmail());
        user.updatePassword(passwordEncoder.encode(requestBody.getPassword()));
        user.updateName(requestBody.getName());
//        user.updateUserInfo(requestBody.getName(), requestBody.getPhoneNumber(), requestBody.getAddress());
        user.updateBalance(requestBody.getBalance());

        userJpaRepository.save(user);
        return UserModifyResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UserModifyResponseDto> modifyMypage(Long id, @Valid UserModifyMypageRequestDto requestBody) {
        User user = userJpaRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateUserInfo(user.getName(), requestBody.getPhoneNumber(), requestBody.getAddress());
        userJpaRepository.save(user);
        return UserModifyResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<AdminModifyResponseDto> modifyUserByAdmin(Long id, AdminModifyRequestDto requestBody) {
        User user = userJpaRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateEmail(requestBody.getEmail());
        user.updatePassword(passwordEncoder.encode(requestBody.getPassword()));
        user.updateBalance(requestBody.getBalance());
        user.setEmailVerified(requestBody.getEmailVerified());
        user.updateRole(requestBody.getRole());

        userJpaRepository.save(user);
        return AdminModifyResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UserDeleteResponseDto> deleteUser(Long id) {
        User user = userJpaRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(false);
        user.updateDeletedAt(ZonedDateTime.now());

        userJpaRepository.save(user);
        return UserDeleteResponseDto.success(UserResponseDto.from(user));
    }

    @Transactional
    @Override
    public Optional<User> getUserByEmail(String currentUserEmail) {
        return userJpaRepository.findByEmail(currentUserEmail);
    }

    @Transactional
    @Override
    public Optional<User> getUserById(Long resourceId) {
        return userJpaRepository.findById(resourceId);
    }
}
