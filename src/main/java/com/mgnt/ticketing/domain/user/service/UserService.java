package com.mgnt.ticketing.domain.user.service;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.controller.user.dto.request.*;
import com.mgnt.ticketing.controller.user.dto.response.*;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    public GetBalanceResponse getBalance(Long userId) {
        return null;
    }

    @Override
    public void charge(Long userId, ChargeRequest request) {

    }

    @Override
    public List<GetMyReservationsResponse> getMyReservations(Long userId) {
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<UpdateUserResponse> modifyUserPassword(Long id, PasswordReques requestBody) {
        User user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(requestBody.getCurrentPassword(), user.getPassword())) {
            return UpdateUserResponse.failure(ErrorCode.CURRENT_PASSWORD_NOT_MATCHED);
        }

        if (!requestBody.getNewPassword1().equals(requestBody.getNewPassword2())) {
            return UpdateUserResponse.failure(ErrorCode.NEW_PASSWORD_NOT_MATCHED);
        }

        user.updatePassword(passwordEncoder.encode(requestBody.getNewPassword1()));
        userJpaRepository.save(user);
        return UpdateUserResponse.success(UserResponseDto.from(user));
    }

    @Override
    public ResponseEntity<GetUserListResponse> getUsers() {
        List<UserResponseDto> users = userJpaRepository.findAllByDeletedAtNull()
                .stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
        return GetUserListResponse.success(users);
    }

    @Override
    public ResponseEntity<GetUserResponse> getUserDetail(Long id) {
        User user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return GetUserResponse.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UpdateUserResponse> modifyUser(Long id, @Valid UserModifyRequest requestBody) {
        User user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 이메일 중복 확인
        if (!user.getEmail().equals(requestBody.getEmail()) && userJpaRepository.existsByEmail(requestBody.getEmail())) {
            return UpdateUserResponse.failure(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        user.updateEmail(requestBody.getEmail());
        user.updatePassword(passwordEncoder.encode(requestBody.getPassword()));
        user.updateName(requestBody.getName());
//        user.updateUserInfo(requestBody.getName(), requestBody.getPhoneNumber(), requestBody.getAddress());
        user.updateBalance(BigDecimal.valueOf(requestBody.getBalance()));

        userJpaRepository.save(user);
        return UpdateUserResponse.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UpdateUserResponse> modifyMypage(Long id, @Valid MypageRequest requestBody) {
        User user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateUserInfo(user.getName(), requestBody.getPhoneNumber(), requestBody.getAddress());
        userJpaRepository.save(user);
        return UpdateUserResponse.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UpdateAllUserResponse> modifyUserByAdmin(Long id, AdminModifyUserRequest requestBody) {
        User user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateEmail(requestBody.getEmail());
        user.updatePassword(passwordEncoder.encode(requestBody.getPassword()));
        user.updateBalance(BigDecimal.valueOf(requestBody.getBalance()));
        user.setEmailVerified(requestBody.getEmailVerified());
        user.updateRole(requestBody.getRole());

        userJpaRepository.save(user);
        return UpdateAllUserResponse.success(UserResponseDto.from(user));
    }

    @Override
    @Transactional
    public ResponseEntity<DeleteUserResponse> deleteUser(Long id) {
        User user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(false);
        user.updateDeletedAt(ZonedDateTime.now());

        userJpaRepository.save(user);
        return DeleteUserResponse.success(UserResponseDto.from(user));
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
