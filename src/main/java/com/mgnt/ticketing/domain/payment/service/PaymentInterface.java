package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.controller.payment.dto.request.CreateRequest;
import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.CreateResponse;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;

/**
 * 결제 서비스 인터페이스
 *
 * 이 인터페이스는 결제와 관련된 기능을 정의합니다.
 */
public interface PaymentInterface {

    /* 결제 요청 */
    PayResponse pay(Long paymentId, PayRequest request);

    /* 결제 정보 생성 */
    CreateResponse create(CreateRequest request);

    /* 결제 취소 */
    CancelPaymentResultResDto cancel(Long paymentId);
}
