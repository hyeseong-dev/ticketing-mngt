package com.mgnt.ticketing.domain.user.service;

import com.mgnt.ticketing.controller.user.dto.request.*;
import com.mgnt.ticketing.controller.user.dto.response.*;
import com.mgnt.ticketing.domain.user.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface UserInterface {

    // 잔액 조회
    GetBalanceResponse getBalance(Long userId);

    // 잔액 충전
    void charge(Long userId, ChargeRequest request);

    // 나의 예약 내역 조회
    List<GetMyReservationsResponse> getMyReservations(Long userId);

    ResponseEntity<GetUserListResponse> getUsers();
    ResponseEntity<GetUserResponse> getUserDetail(Long id);

    ResponseEntity<UpdateAllUserResponse> modifyUserByAdmin(Long id, AdminModifyUserRequest requestBody);
    ResponseEntity<DeleteUserResponse> deleteUser(Long id);

    Optional<User> getUserByEmail(String currentUserEmail);
    Optional<User> getUserById(Long resourceId);

    ResponseEntity<UpdateUserResponse> modifyUser(Long id, UserModifyRequest requestBody);

    ResponseEntity<UpdateUserResponse> modifyMypage(Long id, MypageRequest requestBody);
    ResponseEntity<UpdateUserResponse> modifyUserPassword(Long id, PasswordReques requestBody);
}
