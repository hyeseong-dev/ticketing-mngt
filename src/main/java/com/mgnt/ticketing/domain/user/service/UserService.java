package com.mgnt.ticketing.domain.user.service;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.controller.user.dto.request.*;
import com.mgnt.ticketing.controller.user.dto.response.*;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import com.mgnt.ticketing.domain.user.repository.UserRepository;
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

/**
 * 사용자 서비스 클래스
 *
 * 이 클래스는 사용자와 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserInterface {

    private final UserRepository userRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 잔액 조회
     *
     * @param userId 사용자 ID
     * @return 잔액 응답 DTO
     */
    @Override
    public GetBalanceResponse getBalance(Long userId) {
        User user = userRepository.findById(userId);
        return GetBalanceResponse.from(user);
    }

    /**
     * 사용자 잔액 충전
     *
     * @param userId 사용자 ID
     * @param request 잔액 충전 요청 DTO
     * @return 잔액 응답 DTO
     */
    @Override
    public GetBalanceResponse charge(Long userId, ChargeRequest request) {
        User user = userRepository.findById(userId);
        user = user.chargeBalance(BigDecimal.valueOf(request.amount()));
        return GetBalanceResponse.from(user);
    }

    /**
     * 사용자 비밀번호 수정
     *
     * @param id 사용자 ID
     * @param requestBody 비밀번호 수정 요청 DTO
     * @return 비밀번호 수정 응답 엔티티
     */
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

    /**
     * 모든 사용자 목록 조회
     *
     * @return 사용자 목록 응답 엔티티
     */
    @Override
    public ResponseEntity<GetUserListResponse> getUsers() {
        List<UserResponseDto> users = userJpaRepository.findAllByDeletedAtNull()
                .stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
        return GetUserListResponse.success(users);
    }

    /**
     * 특정 사용자 상세 조회
     *
     * @param id 사용자 ID
     * @return 사용자 상세 응답 엔티티
     */
    @Override
    public ResponseEntity<GetUserResponse> getUserDetail(Long id) {
        User user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return GetUserResponse.success(UserResponseDto.from(user));
    }

    /**
     * 사용자 정보 수정
     *
     * @param id 사용자 ID
     * @param requestBody 사용자 수정 요청 DTO
     * @return 사용자 수정 응답 엔티티
     */
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
        user.updateBalance(BigDecimal.valueOf(requestBody.getBalance()));

        userJpaRepository.save(user);
        return UpdateUserResponse.success(UserResponseDto.from(user));
    }

    /**
     * 마이페이지 정보 수정
     *
     * @param id 사용자 ID
     * @param requestBody 마이페이지 수정 요청 DTO
     * @return 마이페이지 수정 응답 엔티티
     */
    @Override
    @Transactional
    public ResponseEntity<UpdateUserResponse> modifyMypage(Long id, @Valid MypageRequest requestBody) {
        User user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateUserInfo(user.getName(), requestBody.getPhoneNumber(), requestBody.getAddress());
        userJpaRepository.save(user);
        return UpdateUserResponse.success(UserResponseDto.from(user));
    }

    /**
     * 관리자가 사용자 정보 수정
     *
     * @param id 사용자 ID
     * @param requestBody 사용자 수정 요청 DTO
     * @return 사용자 수정 응답 엔티티
     */
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

    /**
     * 사용자 삭제
     *
     * @param id 사용자 ID
     * @return 사용자 삭제 응답 엔티티
     */
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

    /**
     * 이메일로 사용자 조회
     *
     * @param currentUserEmail 현재 사용자 이메일
     * @return 사용자 객체 Optional
     */
    @Transactional
    @Override
    public Optional<User> getUserByEmail(String currentUserEmail) {
        return userJpaRepository.findByEmail(currentUserEmail);
    }

    /**
     * 사용자 ID로 사용자 조회
     *
     * @param resourceId 사용자 ID
     * @return 사용자 객체 Optional
     */
    @Transactional
    @Override
    public Optional<User> getUserById(Long resourceId) {
        return userJpaRepository.findById(resourceId);
    }
}
