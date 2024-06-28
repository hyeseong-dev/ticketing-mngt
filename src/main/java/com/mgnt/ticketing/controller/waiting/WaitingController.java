package com.mgnt.ticketing.controller.waiting;

import com.mgnt.ticketing.base.exception.ApiResult;
import com.mgnt.ticketing.controller.waiting.request.AddWaitingQueueRequest;
import com.mgnt.ticketing.controller.waiting.request.IssueTokenRequest;
import com.mgnt.ticketing.controller.waiting.response.CheckActiveResponse;
import com.mgnt.ticketing.controller.waiting.response.IssueTokenResponse;
import com.mgnt.ticketing.domain.waiting.service.WaitingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대기열 관련 API 컨트롤러
 *
 * 이 클래스는 대기열과 관련된 API 요청을 처리합니다.
 */
@Tag(name = "대기열", description = "waiting-controller")
@RequestMapping("/waits")
@RestController
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService service;

    /**
     * 토큰 발급
     *
     * @param request 토큰 발급 요청 DTO
     * @return 발급된 토큰 응답
     */
    @Operation(summary = "토큰 발급")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = IssueTokenResponse.class)))
    @PostMapping(value = "/issue-token")
    public ApiResult<IssueTokenResponse> issueToken(@RequestBody @Valid IssueTokenRequest request) {
        return ApiResult.success(service.issueToken(request.userId()));
    }

    /**
     * 대기열 저장
     *
     * 첫 진입 시 또는 새로고침 시 호출
     *
     * @param request 대기열 저장 요청 DTO
     * @return 활성 상태 확인 응답
     */
    // TODO 대기열 저장, 확인 호출 하나로 통합해보자
    // 없으면 넣고, 있으면 확인하고, 만료되면 만료되었다고 반환하면 되지 않을까?
    // 대기열 범용성 포인트 : 호출 url도 같이 관리? 토큰에? 테이블 컬럼으로?
    @Operation(summary = "대기열 저장", description = "첫 진입 시 또는 새로고침 시 호출")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CheckActiveResponse.class)))
    @PostMapping(value = "/queue")
    public ApiResult<CheckActiveResponse> addWaitingQueue(@RequestBody @Valid AddWaitingQueueRequest request) {
        return ApiResult.success(service.addWaitingQueue(request.userId(), request.token()));
    }

    /**
     * 대기열 확인
     *
     * 대기 시 호출 (polling 방식)
     *
     * @param request 대기열 확인 요청 DTO
     * @return 활성 상태 확인 응답
     */
    @Operation(summary = "대기열 확인", description = "대기 시 호출 (polling 방식)")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CheckActiveResponse.class)))
    @PostMapping(value = "/check")
    public ApiResult<CheckActiveResponse> checkActive(@RequestBody @Valid AddWaitingQueueRequest request) {
        return ApiResult.success(service.checkActive(request.userId(), request.token()));
    }
}
