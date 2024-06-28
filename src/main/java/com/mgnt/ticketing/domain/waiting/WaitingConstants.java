package com.mgnt.ticketing.domain.waiting;

/**
 * 대기열 관련 상수 정의 클래스
 */
public final class WaitingConstants {

    /* [활성 유저 수]
    티켓 프로젝트에서 이는 동시에 티켓 대기열에 있는 사용자 중 최대 50명까지 활성 상태로 관리될 수 있다는 것을 나타냅니다.
    활성 유저 수가 50명을 초과하면 새로운 사용자들은 대기열로 전환되거나 다른 방식으로 관리될 수 있습니다.
    이를 통해 시스템의 부하를 관리하고, 티켓 발급 절차를 효율적으로 운영할 수 있습니다.
     */
    public static final int ACTIVE_USER_CNT = 50;

    //[ 대기열 자동 만료 시간 (5분)]
    /*
    유저가 대기열에 추가된 후 5분이 지나면 자동으로 대기열에서 만료되어 더 이상 활성 상태가 아니게 됩니다.
    이를 통해 대기열에서 오래된 요청들을 정리하고,
    시스템의 부하를 관리하며 새로운 유저가 대기열에 원활히 진입할 수 있도록 합니다
     */
    public static final long AUTO_EXPIRED_MILLIS = 5 * 60 * 1000;

}