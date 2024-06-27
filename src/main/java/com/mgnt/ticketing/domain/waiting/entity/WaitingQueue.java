package com.mgnt.ticketing.domain.waiting.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * 대기 엔티티 클래스
 *
 * 이 클래스는 대기 정보를 나타내며, 데이터베이스의 'waiting_queue' 테이블과 매핑됩니다.
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "waiting_queue")
public class WaitingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long waitingQueueId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private Timestamp activeTime;

    private Timestamp requestTime = new Timestamp(System.currentTimeMillis());

    /**
     * 활성 상태의 엔티티 생성 메서드
     *
     * @param userId 사용자 ID
     * @param token  토큰
     * @return 활성 상태의 WaitingQueue 객체
     */
    public static WaitingQueue toActiveEntity(Long userId, String token) {
        return WaitingQueue.builder()
                .userId(userId)
                .token(token)
                .status(Status.ACTIVE)
                .activeTime(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    /**
     * 대기 상태의 엔티티 생성 메서드
     *
     * @param userId 사용자 ID
     * @param token  토큰
     * @return 대기 상태의 WaitingQueue 객체
     */
    public static WaitingQueue toWaitingEntity(Long userId, String token) {
        return WaitingQueue.builder()
                .userId(userId)
                .token(token)
                .status(Status.WAITING)
                .build();
    }

    /**
     * 토큰 만료 상태로 변경하는 메서드
     */
    public void expiredToken() {
        this.status = Status.EXPIRED;
    }

    /**
     * 대기 상태를 활성 상태로 변경하는 메서드
     */
    public void toActive() {
        this.status = Status.ACTIVE;
    }

    public enum Status {
        EXPIRED,
        ACTIVE,
        WAITING
    }

    @Builder
    public WaitingQueue(Long waitingQueueId, Long userId, String token, Status status, Timestamp activeTime, Timestamp requestTime) {
        this.waitingQueueId = waitingQueueId;
        this.userId = userId;
        this.token = token;
        this.status = status;
        this.activeTime = activeTime;
        this.requestTime = requestTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaitingQueue that = (WaitingQueue) o;
        return Objects.equals(waitingQueueId, that.waitingQueueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waitingQueueId);
    }
}
