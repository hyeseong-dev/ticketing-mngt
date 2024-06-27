package com.mgnt.ticketing.domain.user.service;

import com.mgnt.ticketing.controller.user.dto.request.*;
import com.mgnt.ticketing.controller.user.dto.response.*;
import com.mgnt.ticketing.domain.user.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 서비스 인터페이스
 *
 * 이 인터페이스는 사용자와 관련된 비즈니스 로직을 정의합니다.
 */
public interface UserInterface {

    /**
     * 잔액 조회
     *
     * @param userId 사용자 ID
     * @return 잔액 응답 DTO
     */
    GetBalanceResponse getBalance(Long userId);

    /**
     * 잔액 충전
     *
     * @param userId 사용자 ID
     * @param request 잔액 충전 요청 DTO
     * @return 잔액 응답 DTO
     */
    GetBalanceResponse charge(Long userId, ChargeRequest request);

    /**
     * 모든 사용자 목록 조회
     *
     * @return 사용자 목록 응답 엔티티
     */
    ResponseEntity<GetUserListResponse> getUsers();

    /**
     * 특정 사용자 상세 조회
     *
     * @param id 사용자 ID
     * @return 사용자 상세 응답 엔티티
     */
    ResponseEntity<GetUserResponse> getUserDetail(Long id);

    /**
     * 관리자가 사용자 정보 수정
     *
     * @param id 사용자 ID
     * @param requestBody 사용자 수정 요청 DTO
     * @return 사용자 수정 응답 엔티티
     */
    ResponseEntity<UpdateAllUserResponse> modifyUserByAdmin(Long id, AdminModifyUserRequest requestBody);

    /**
     * 사용자 삭제
     *
     * @param id 사용자 ID
     * @return 사용자 삭제 응답 엔티티
     */
    ResponseEntity<DeleteUserResponse> deleteUser(Long id);

    /**
     * 이메일로 사용자 조회
     *
     * @param currentUserEmail 현재 사용자 이메일
     * @return 사용자 객체 Optional
     */
    Optional<User> getUserByEmail(String currentUserEmail);

    /**
     * 사용자 ID로 사용자 조회
     *
     * @param resourceId 사용자 ID
     * @return 사용자 객체 Optional
     */
    Optional<User> getUserById(Long resourceId);

    /**
     * 사용자 정보 수정
     *
     * @param id 사용자 ID
     * @param requestBody 사용자 수정 요청 DTO
     * @return 사용자 수정 응답 엔티티
     */
    ResponseEntity<UpdateUserResponse> modifyUser(Long id, UserModifyRequest requestBody);

    /**
     * 마이페이지 정보 수정
     *
     * @param id 사용자 ID
     * @param requestBody 마이페이지 수정 요청 DTO
     * @return 마이페이지 수정 응답 엔티티
     */
    ResponseEntity<UpdateUserResponse> modifyMypage(Long id, MypageRequest requestBody);

    /**
     * 사용자 비밀번호 수정
     *
     * @param id 사용자 ID
     * @param requestBody 비밀번호 수정 요청 DTO
     * @return 비밀번호 수정 응답 엔티티
     */
    ResponseEntity<UpdateUserResponse> modifyUserPassword(Long id, PasswordReques requestBody);
}
