package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.payment.service.dto.CreatePaymentReqDto;

/**
 * 결제 서비스 인터페이스
 *
 * 이 인터페이스는 결제와 관련된 기능을 정의합니다.
 */
public interface PaymentInterface {

    /**
     * 결제 요청
     *
     * @param paymentId 결제 ID
     * @param request 결제 요청 DTO
     * @return 결제 응답 DTO
     */
    PayResponse pay(Long paymentId, PayRequest request);

    /**
     * 결제 정보 생성
     *
     * @param reqDto 결제 생성 요청 DTO
     * @return 생성된 결제 정보
     */
    Payment create(CreatePaymentReqDto reqDto);

    /**
     * 결제 취소
     *
     * @param paymentId 결제 ID
     * @return 결제 취소 결과 응답 DTO
     */
    CancelPaymentResultResDto cancel(Long paymentId);
}
