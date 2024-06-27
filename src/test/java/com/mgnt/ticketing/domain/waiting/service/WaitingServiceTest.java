package com.mgnt.ticketing.domain.waiting.service;

import com.mgnt.ticketing.base.jwt.JwtService;
import com.mgnt.ticketing.controller.waiting.response.CheckActiveResponse;
import com.mgnt.ticketing.controller.waiting.response.IssueTokenResponse;
import com.mgnt.ticketing.domain.waiting.entity.WaitingQueue;
import com.mgnt.ticketing.domain.waiting.repository.WaitingQueueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.when;


class WaitingServiceTest {

    private WaitingService waitingService;
    private WaitingQueueRepository waitingQueueRepository;
    private JwtService jwtService;

    private String 토큰;
    private WaitingQueue 활성유저;
    private WaitingQueue 대기유저;

    @BeforeEach
    void setUp() {
        // mocking
        waitingQueueRepository = Mockito.mock(WaitingQueueRepository.class);
        jwtService = Mockito.mock(JwtService.class);

        waitingService = new WaitingService(waitingQueueRepository, jwtService);

        // 세팅
        토큰 = "023ADO=fASDF234ji%fAKF=";
        활성유저 = WaitingQueue.toActiveEntity(1L, 토큰);
        대기유저 = WaitingQueue.toWaitingEntity(1L, 토큰);
    }

    @Test
    @DisplayName("토큰_생성")
    void issueTokenTest_토큰_생성() {
        // given
        Long userId = 1L;

        // when
        when(jwtService.createToken(userId)).thenReturn(토큰);
        IssueTokenResponse response = waitingService.issueToken(userId);

        // then
        assertThat(response.token()).isEqualTo(토큰);
    }

    @Test
    @DisplayName("대기열_추가_활성유저")
    void addWaitingQueueTest_대기열_추가_활성유저() {
        // given
        Long userId = 1L;
        String token = 토큰;

        // when
        when(waitingQueueRepository.findByUserId(userId)).thenReturn(null);
        when(waitingQueueRepository.countByStatusIs(any())).thenReturn(15L);
        CheckActiveResponse response = waitingService.addWaitingQueue(userId, token);

        // then
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("대기열_추가_대기유저")
    void addWaitingQueueTest_대기열_추가_대기유저() {
        // given
        Long userId = 1L;
        String token = 토큰;

        // when
        when(waitingQueueRepository.findByUserId(userId)).thenReturn(null);
        when(waitingQueueRepository.countByStatusIs(WaitingQueue.Status.ACTIVE)).thenReturn(50L);
        when(waitingQueueRepository.countByStatusIs(WaitingQueue.Status.WAITING)).thenReturn(9L);
        CheckActiveResponse response = waitingService.addWaitingQueue(userId, token);

        // then
        assertThat(response.isActive()).isFalse();
        assertThat(response.waitingTicketInfo().waitingNum()).isEqualTo(9L);
        assertThat(response.waitingTicketInfo().expectedWaitTimeInSeconds()).isEqualTo(540);
    }

    @Test
    @DisplayName("대기열_상태_확인_대기열_정보_없거나_만료됨")
    void checkActiveTest_대기열_상태_확인_대기열_정보_없거나_만료됨() {
        // given
        Long userId = 1L;
        String token = 토큰;

        // when
        when(waitingQueueRepository.findByUserIdAndToken(userId, token)).thenReturn(null);

        // then
        EntityNotFoundException expected = assertThrows(EntityNotFoundException.class, () ->
                waitingService.checkActive(userId, token));
        assertThat(expected.getMessage()).isEqualTo("새로고침하여 다시 진입하세요.");
    }

    @Test
    @DisplayName("대기열_상태_확인_활성유저")
    void checkActiveTest_대기열_상태_확인_활성유저() {
        // given
        Long userId = 1L;
        String token = 토큰;

        // when
        when(waitingQueueRepository.findByUserIdAndToken(userId, token)).thenReturn(활성유저);
        CheckActiveResponse response = waitingService.checkActive(userId, token);

        // then
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("대기열_상태_확인_대기유저")
    void checkActiveTest_대기열_상태_확인_대기유저() {
        // given
        Long userId = 1L;
        String token = 토큰;

        // when
        when(waitingQueueRepository.findByUserIdAndToken(userId, token)).thenReturn(대기유저);
        when(waitingQueueRepository.countByRequestTimeBeforeAndStatusIs(any(), any())).thenReturn(15L);
        CheckActiveResponse response = waitingService.checkActive(userId, token);

        // then
        assertThat(response.isActive()).isFalse();
        assertThat(response.waitingTicketInfo().waitingNum()).isEqualTo(15L);
    }
}