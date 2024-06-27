package com.mgnt.ticketing.domain.waiting;

/**
 * 대기열 관련 상수 정의 클래스
 */
public final class WaitingConstants {

    // 활성 유저 수
    public static final int ACTIVE_USER_CNT = 50;

    // 대기열 자동 만료 시간 (5분)
    public static final long AUTO_EXPIRED_MILLIS = 5 * 60 * 1000;
}