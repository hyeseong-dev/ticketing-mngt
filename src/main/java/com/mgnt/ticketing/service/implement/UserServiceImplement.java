package com.mgnt.ticketing.service.implement;

import com.mgnt.ticketing.common.error.ErrorCode;
import com.mgnt.ticketing.dto.request.user.AdminModifyRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyMypageRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyPasswordRequestDto;
import com.mgnt.ticketing.dto.request.user.UserModifyRequestDto;
import com.mgnt.ticketing.dto.response.user.*;
import com.mgnt.ticketing.entity.UserEntity;
import com.mgnt.ticketing.repository.UserRepository;
import com.mgnt.ticketing.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImplement implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ResponseEntity<UserModifyResponseDto> modifyUserPassword(Long id, UserModifyPasswordRequestDto requestBody) {
        UserEntity user = userRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(requestBody.getCurrentPassword(), user.getPassword())) {
            return UserModifyResponseDto.failure(ErrorCode.CURRENT_PASSWORD_NOT_MATCHED);
        }

        if (!requestBody.getNewPassword1().equals(requestBody.getNewPassword2())) {
            return UserModifyResponseDto.failure(ErrorCode.NEW_PASSWORD_NOT_MATCHED);
        }

        user.setPassword(passwordEncoder.encode(requestBody.getNewPassword1()));
        userRepository.save(user);
        return UserModifyResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    public ResponseEntity<UserListResponseDto> getUsers() {
        List<UserResponseDto> users = userRepository.findAllByDeletedAtNull()
                .stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
        return UserListResponseDto.success(users);
    }

    @Override
    public ResponseEntity<UserDetailResponseDto> getUserDetail(Long id) {
        UserEntity user = userRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDetailResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UserModifyResponseDto> modifyUser(Long id, @Valid UserModifyRequestDto requestBody) {
        UserEntity user = userRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 이메일 중복 확인
        if (!user.getEmail().equals(requestBody.getEmail()) && userRepository.existsByEmail(requestBody.getEmail())) {
            return UserModifyResponseDto.failure(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        user.setEmail(requestBody.getEmail());
        user.setPassword(passwordEncoder.encode(requestBody.getPassword()));
        user.setName(requestBody.getName());
        user.setPoints(requestBody.getPoints());

        userRepository.save(user);
        return UserModifyResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UserModifyResponseDto> modifyMypage(Long id, @Valid UserModifyMypageRequestDto requestBody) {
        UserEntity user = userRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAddress(requestBody.getAddress());
        user.setPhoneNumber(requestBody.getPhoneNumber());
        userRepository.save(user);
        return UserModifyResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<AdminModifyResponseDto> modifyUserByAdmin(Long id, AdminModifyRequestDto requestBody) {
        UserEntity user = userRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(requestBody.getEmail());
        user.setPassword(passwordEncoder.encode(requestBody.getPassword()));
        user.setPoints(requestBody.getPoints());
        user.setEmailVerified(requestBody.getEmailVerified());
        user.setRole(requestBody.getRole());

        userRepository.save(user);
        return AdminModifyResponseDto.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UserDeleteResponseDto> deleteUser(Long id) {
        UserEntity user = userRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(false);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
        return UserDeleteResponseDto.success(UserResponseDto.from(user));
    }

    @Transactional
    @Override
    public Optional<UserEntity> getUserByEmail(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail);
    }

    @Transactional
    @Override
    public Optional<UserEntity> getUserById(Long resourceId) {
        return userRepository.findById(resourceId);
    }
}
