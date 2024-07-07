package com.mgnt.userservice.controller;

import com.mgnt.core.exception.ApiResult;
import com.mgnt.userservice.controller.dto.request.ChargeRequest;
import com.mgnt.userservice.controller.dto.request.MypageRequest;
import com.mgnt.userservice.controller.dto.request.PasswordRequet;
import com.mgnt.userservice.controller.dto.request.UserModifyRequest;
import com.mgnt.userservice.controller.dto.response.DeleteUserResponse;
import com.mgnt.userservice.controller.dto.response.GetBalanceResponse;
import com.mgnt.userservice.controller.dto.response.GetUserResponse;
import com.mgnt.userservice.domain.service.UserInterface;
import com.mgnt.userservice.domain.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserInterface userInterface;
    ;

    /**
     * 사용자 충전
     *
     * @param userId  사용자 ID
     * @param request 충전 요청 정보
     */
    @PatchMapping("/{userId}/charge")
    public ApiResult<GetBalanceResponse> charge(@PathVariable(value = "userId") @NotNull Long userId,
                                                @RequestBody @Valid ChargeRequest request) {
        return ApiResult.success(service.charge(userId, request));
    }

    /**
     * 사용자 잔액 조회
     *
     * @param userId 사용자 ID
     * @return 잔액 정보
     */
    @GetMapping("/{userId}/balance")
    public ApiResult<GetBalanceResponse> getBalance(@PathVariable(value = "userId") @NotNull Long userId) {
        return ApiResult.success(service.getBalance(userId));
    }

    /**
     * 사용자 상세 정보 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 상세 정보 응답
     */
    @GetMapping("/{userId}")
    public ApiResult<GetUserResponse> getUserDetail(@PathVariable(value = "userId") Long userId) {
        return ApiResult.success(service.getUserDetail(userId));
    }

    /**
     * 사용자 정보 수정
     *
     * @param userId      사용자 ID
     * @param requestBody 수정 요청 정보
     * @return 수정된 사용자 정보 응답
     */
    @PatchMapping("/{id}")
    public ApiResult<GetUserResponse> modifyUser(
            @PathVariable(value = "userId") Long userId,
            @RequestBody @Valid UserModifyRequest requestBody
    ) {
        return ApiResult.success(service.modifyUser(userId));
    }

    /**
     * 마이페이지 정보 수정
     *
     * @param requestBody 수정 요청 정보
     * @return 수정된 사용자 정보 응답
     */
    @PatchMapping("/mypage")
    public ResponseEntity<UpdateUserResponse> modifyMypage(
            Authentication authentication,
            @RequestBody @Valid MypageRequest requestBody
    ) {
        System.out.println(authentication);
        long id = 1L;
        return userInterface.modifyMypage(id, requestBody);
    }

    /**
     * 사용자 비밀번호 수정
     *
     * @param id          사용자 ID
     * @param requestBody 비밀번호 수정 요청 정보
     * @return 수정된 사용자 정보 응답
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<?> modifyPassword(
            @PathVariable Long id,
            @RequestBody @Valid PasswordRequet requestBody
    ) {
        return userInterface.modifyUserPassword(id, requestBody);
    }

    /**
     * 사용자 삭제
     *
     * @param id 사용자 ID
     * @return 삭제된 사용자 정보 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteUserResponse> deleteUser(
            @PathVariable Long id
    ) {
        return userInterface.deleteUser(id);
    }
}
