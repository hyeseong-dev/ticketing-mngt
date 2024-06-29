package com.mgnt.ticketing.controller.user;

import com.mgnt.ticketing.base.exception.ApiResult;
import com.mgnt.ticketing.controller.user.dto.request.ChargeRequest;
import com.mgnt.ticketing.controller.user.dto.request.MypageRequest;
import com.mgnt.ticketing.controller.user.dto.request.PasswordReques;
import com.mgnt.ticketing.controller.user.dto.request.UserModifyRequest;
import com.mgnt.ticketing.controller.user.dto.response.*;
import com.mgnt.ticketing.domain.user.service.UserInterface;
import com.mgnt.ticketing.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API 컨트롤러
 */
@Tag(name = "유저", description = "user-controller")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserInterface userInterface;
    private final String resourceType = "USER";

    /**
     * 사용자 충전
     *
     * @param userId 사용자 ID
     * @param request 충전 요청 정보
     */
    @Operation(summary = "잔액 충전")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GetBalanceResponse.class)))
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
    @Operation(summary = "잔액 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GetBalanceResponse.class)))
    @GetMapping("/{userId}/balance")
    public ApiResult<GetBalanceResponse> getBalance(@PathVariable(value = "userId") @NotNull Long userId) {
        return ApiResult.success(service.getBalance(userId));
    }

    /**
     * 사용자 상세 정보 조회
     *
     * @param id 사용자 ID
     * @return 사용자 상세 정보 응답
     */
    @Operation(summary = "사용자 상세 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GetUserResponse.class)))
    @GetMapping("/{userId}")
    public ResponseEntity<GetUserResponse> getUserDetail(@PathVariable(value = "userId") Long id) {
        return userInterface.getUserDetail(id);
    }

    /**
     * 사용자 정보 수정
     *
     * @param id 사용자 ID
     * @param requestBody 수정 요청 정보
     * @return 수정된 사용자 정보 응답
     */
    @Operation(summary = "사용자 정보 수정")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UpdateUserResponse.class)))
    @PatchMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> modifyUser(
            @PathVariable(value = "userId") Long id,
            @RequestBody @Valid UserModifyRequest requestBody
    ) {
        return userInterface.modifyUser(id, requestBody);
    }

    /**
     * 마이페이지 정보 수정
     *
     *
     * @param requestBody 수정 요청 정보
     * @return 수정된 사용자 정보 응답
     */
    @Operation(summary = "마이페이지 정보 수정")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UpdateUserResponse.class)))
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
     * @param id 사용자 ID
     * @param requestBody 비밀번호 수정 요청 정보
     * @return 수정된 사용자 정보 응답
     */
    @Operation(summary = "비밀번호 수정")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UpdateUserResponse.class)))
    @PatchMapping("/{id}/password")
    public ResponseEntity<UpdateUserResponse> modifyPassword(
            @PathVariable Long id,
            @RequestBody @Valid PasswordReques requestBody
    ) {
        return userInterface.modifyUserPassword(id, requestBody);
    }

    /**
     * 사용자 삭제
     *
     * @param id 사용자 ID
     * @return 삭제된 사용자 정보 응답
     */
    @Operation(summary = "사용자 삭제")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DeleteUserResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteUserResponse> deleteUser(
            @PathVariable Long id
    ) {
        return userInterface.deleteUser(id);
    }
}
