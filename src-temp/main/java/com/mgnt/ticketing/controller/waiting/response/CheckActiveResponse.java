package com.mgnt.ticketing.controller.waiting.response;

/**
 * 활성 상태 확인 응답 DTO
 *
 * @param isActive 활성 상태 여부
 * @param waitingTicketInfo 대기 상태일 경우 대기 정보
 */
public record CheckActiveResponse(
        // 활성 상태 여부
        boolean isActive,
        // 대기 상태일 경우 대기 정보 반환
        WaitingTicketInfo waitingTicketInfo
) {

    /**
     * 대기 티켓 정보 DTO
     *
     * @param waitingNum 대기 번호
     * @param expectedWaitTimeInSeconds 예상 대기 시간 (초)
     */
    public record WaitingTicketInfo(
            Long waitingNum,
            Long expectedWaitTimeInSeconds
    ) {
    }
}
