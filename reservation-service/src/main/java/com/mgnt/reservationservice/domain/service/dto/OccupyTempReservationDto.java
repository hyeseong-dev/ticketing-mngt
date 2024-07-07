package com.mgnt.reservationservice.domain.service.dto;

/**
 * 임시 예약 점유 DTO
 * <p>
 * 이 클래스는 임시로 예약을 점유하는 정보를 나타냅니다.
 *
 * @param reservationId 예약 ID
 * @param occupyTime    점유 시간 (밀리초)
 */
public record OccupyTempReservationDto(
        Long reservationId,
        long occupyTime
) {

    /**
     * 예약을 임시로 점유하기 위한 정적 팩토리 메서드
     *
     * @param reservationId 예약 ID
     * @return 임시 예약 점유 DTO
     */
    public static OccupyTempReservationDto toOccupy(Long reservationId) {
        return new OccupyTempReservationDto(reservationId, System.currentTimeMillis());
    }
}