package com.mgnt.ticketing.controller.user;

import com.mgnt.ticketing.base.util.aop.CheckAccess;
import com.mgnt.ticketing.controller.user.dto.request.ChargeRequest;
import com.mgnt.ticketing.controller.user.dto.request.MypageRequest;
import com.mgnt.ticketing.controller.user.dto.request.PasswordReques;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyRequest;
import com.mgnt.ticketing.controller.user.dto.response.*;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.reservation.ReservationEnums;
import com.mgnt.ticketing.domain.user.service.UserInterface;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserInterface userInterface;
    private final String resourceType = "USER";

    @GetMapping("/{userId}/reservation")
    public List<GetMyReservationsResponse> getMyReservation(@PathVariable(value = "userId") @NotNull Long userId) {
        // dummy data
        return List.of(GetMyReservationsResponse.builder()
                .reservationId(1L)
                .status(ReservationEnums.Status.ING)
                .concertInfo(GetMyReservationsResponse.ConcertInfo.builder()
                        .concertId(1L)
                        .concertDateId(1L)
                        .name("2024 테스트 콘서트 in 서울")
                        .date(ZonedDateTime.now().plusMonths(1))
                        .seatId(2L)
                        .seatNum(2)
                        .build())
                .paymentInfo(GetMyReservationsResponse.PaymentInfo.builder()
                        .paymentId(1L)
                        .status(PaymentEnums.Status.READY)
                        .paymentPrice(79000)
                        .build())
                .build());
    }

    @PatchMapping("/{userId}/charge")
    public void charge(@PathVariable(value = "userId") @NotNull Long userId,
                       @RequestBody @Valid ChargeRequest request
    ) {

    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @GetMapping("/{userId}/balance")
    public GetBalanceResponse getBalance(@PathVariable(value = "userId") @NotNull Long userId) {
        // dummy data
        return new GetBalanceResponse(1000);
    }

    @CheckAccess(roles = {"ADMIN"}, resourceType = resourceType)
    @GetMapping
    public ResponseEntity<GetUserListResponse> getUsers() {
        return userInterface.getUsers();
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @GetMapping("/{id}")
    public ResponseEntity<GetUserResponse> getUserDetail(@PathVariable Long id) {
        return userInterface.getUserDetail(id);
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @PatchMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> modifyUser(
            @PathVariable Long id,
            @RequestBody @Valid UserModifyRequest requestBody
    ) {
        return userInterface.modifyUser(id, requestBody);
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @PatchMapping("/mypage/{id}")
    public ResponseEntity<UpdateUserResponse> modifyMypage(
            @PathVariable Long id,
            @RequestBody @Valid MypageRequest requestBody
    ) {
        return userInterface.modifyMypage(id, requestBody);
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @PatchMapping("/{id}/password")
    public ResponseEntity<UpdateUserResponse> modifyPassword(
            @PathVariable Long id,
            @RequestBody @Valid PasswordReques requestBody
    ) {
        return userInterface.modifyUserPassword(id, requestBody);
    }

    @CheckAccess(roles = {"ADMIN", "USER"}, resourceType = resourceType)
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteUserResponse> deleteUser(
            @PathVariable Long id
    ) {
        return userInterface.deleteUser(id);
    }

}
