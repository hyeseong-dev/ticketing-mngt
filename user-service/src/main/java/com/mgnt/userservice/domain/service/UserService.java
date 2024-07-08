package com.mgnt.userservice.domain.service;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.userservice.controller.dto.request.*;
import com.mgnt.userservice.controller.dto.response.*;
import com.mgnt.userservice.domain.entity.Users;
import com.mgnt.userservice.domain.repository.UserJpaRepository;
import com.mgnt.userservice.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
 * <p>
 * 이 클래스는 사용자와 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

//    /**
//     * 사용자 잔액 조회
//     *
//     * @param userId 사용자 ID
//     * @return 잔액 응답 DTO
//     */
//    public GetBalanceResponse getBalance(Long userId) {
//        Users user = userRepository.findById(userId);
//        return GetBalanceResponse.from(user);
//    }
//
//    /**
//     * 사용자 잔액 충전
//     *
//     * @param userId  사용자 ID
//     * @param request 잔액 충전 요청 DTO
//     * @return 잔액 응답 DTO
//     */
//    @Override
//    public GetBalanceResponse charge(Long userId, ChargeRequest request) {
//        Users user = userRepository.findById(userId);
//        user = user.chargeBalance(BigDecimal.valueOf(request.amount()));
//        return GetBalanceResponse.from(user);
//    }
//
//    /**
//     * 사용자 비밀번호 수정
//     *
//     * @param id          사용자 ID
//     * @param requestBody 비밀번호 수정 요청 DTO
//     * @return 비밀번호 수정 응답 엔티티
//     */
//    @Override
//    @Transactional
//    public void modifyUserPassword(Long id, PasswordRequet requestBody) {
//        Users user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//        if (!passwordEncoder.matches(requestBody.getCurrentPassword(), user.getPassword())) {
//            return UpdateUserResponse.failure(ErrorCode.CURRENT_PASSWORD_NOT_MATCHED);
//        }
//
//        if (!requestBody.getNewPassword1().equals(requestBody.getNewPassword2())) {
//            return UpdateUserResponse.failure(ErrorCode.NEW_PASSWORD_NOT_MATCHED);
//        }
//
//        user.updatePassword(passwordEncoder.encode(requestBody.getNewPassword1()));
//        userJpaRepository.save(user);
//        return UpdateUserResponse.success(UserResponseDto.from(user));
//    }
//
//    /**
//     * 모든 사용자 목록 조회
//     *
//     * @return 사용자 목록 응답 엔티티
//     */
//    @Override
//    public ResponseEntity<GetUserListResponse> getUsers() {
//        List<UserResponseDto> users = userJpaRepository.findAllByDeletedAtNull()
//                .stream()
//                .map(UserResponseDto::from)
//                .collect(Collectors.toList());
//        return GetUserListResponse.success(users);
//    }

    /**
     * 특정 사용자 상세 조회
     *
     * @param id 사용자 ID
     * @return 사용자 상세 응답 엔티티
     */
    public GetUserResponse getUserDetail(Long id) {
        Users user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return GetUserResponse.from(user);
    }

//    /**
//     * 사용자 정보 수정
//     *
//     * @param id          사용자 ID
//     * @param requestBody 사용자 수정 요청 DTO
//     * @return 사용자 수정 응답 엔티티
//     */
//    @Override
//    @Transactional
//    public ResponseEntity<UpdateUserResponse> modifyUser(Long id, @Valid UserModifyRequest requestBody) {
//        Users user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//         이메일 중복 확인
//        if (!user.getEmail().equals(requestBody.getEmail()) && userJpaRepository.existsByEmail(requestBody.getEmail())) {
//            return UpdateUserResponse.failure(ErrorCode.EMAIL_ALREADY_EXISTS);
//        }
//
//        user.updateEmail(requestBody.getEmail());
//        user.updatePassword(passwordEncoder.encode(requestBody.getPassword()));
//        user.updateName(requestBody.getName());
//        user.updateBalance(BigDecimal.valueOf(requestBody.getBalance()));
//
//        userJpaRepository.save(user);
//        return UpdateUserResponse.success(UserResponseDto.from(user));
//    }
//
//    /**
//     * 마이페이지 정보 수정
//     *
//     * @param id          사용자 ID
//     * @param requestBody 마이페이지 수정 요청 DTO
//     * @return 마이페이지 수정 응답 엔티티
//     */
//    @Override
//    @Transactional
//    public ResponseEntity<UpdateUserResponse> modifyMypage(Long id, @Valid MypageRequest requestBody) {
//        Users user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//        user.updateUserInfo(user.getName(), requestBody.getPhoneNumber(), requestBody.getAddress());
//        userJpaRepository.save(user);
//        return UpdateUserResponse.success(UserResponseDto.from(user));
//    }
//
//    /**
//     * 관리자가 사용자 정보 수정
//     *
//     * @param id          사용자 ID
//     * @param requestBody 사용자 수정 요청 DTO
//     * @return 사용자 수정 응답 엔티티
//     */
//    @Override
//    @Transactional
//    public ResponseEntity<UpdateAllUserResponse> modifyUserByAdmin(Long id, AdminModifyUserRequest requestBody) {
//        Users user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//        user.updateEmail(requestBody.getEmail());
//        user.updatePassword(passwordEncoder.encode(requestBody.getPassword()));
//        user.updateBalance(BigDecimal.valueOf(requestBody.getBalance()));
//        user.setEmailVerified(requestBody.getEmailVerified());
//        user.updateRole(requestBody.getRole());
//
//        userJpaRepository.save(user);
//        return UpdateAllUserResponse.success(UserResponseDto.from(user));
//    }
//
//    /**
//     * 사용자 삭제
//     *
//     * @param id 사용자 ID
//     * @return 사용자 삭제 응답 엔티티
//     */
//    @Override
//    @Transactional
//    public ResponseEntity<DeleteUserResponse> deleteUser(Long id) {
//        Users user = userJpaRepository.findByUserIdAndDeletedAtNull(id)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//        user.setEmailVerified(false);
//        user.updateDeletedAt(ZonedDateTime.now());
//
//        userJpaRepository.save(user);
//        return DeleteUserResponse.success(UserResponseDto.from(user));
//    }
//
//    /**
//     * 이메일로 사용자 조회
//     *
//     * @param currentUserEmail 현재 사용자 이메일
//     * @return 사용자 객체 Optional
//     */
//    @Transactional
//    @Override
//    public Optional<Users> getUserByEmail(String currentUserEmail) {
//        return userJpaRepository.findByEmail(currentUserEmail);
//    }
//
//    /**
//     * 사용자 ID로 사용자 조회
//     *
//     * @param resourceId 사용자 ID
//     * @return 사용자 객체 Optional
//     */
//    @Transactional
//    @Override
//    public Optional<Users> getUserById(Long resourceId) {
//        return userJpaRepository.findById(resourceId);
//    }
}
