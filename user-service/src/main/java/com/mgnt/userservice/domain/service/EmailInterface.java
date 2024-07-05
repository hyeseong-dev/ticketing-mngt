package com.mgnt.userservice.domain.service;

import com.mgnt.userservice.controller.dto.request.EmailVerificationRequestDto;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailInterface {

    /**
     * 이메일 인증 코드를 생성하고 사용자에게 발송합니다.
     *
     * @param email 사용자의 이메일 주소
     * @param name  사용자의 이름
     * @throws MessagingException           이메일 발송 중 오류 발생 시
     * @throws UnsupportedEncodingException 인코딩 관련 오류 발생 시
     */
    void sendVerificationEmail(String email, String name) throws MessagingException, UnsupportedEncodingException;

    /**
     * 사용자가 제공한 이메일 인증 코드의 유효성을 검증합니다.
     *
     * @param email 사용자의 이메일 주소
     * @param code  사용자가 입력한 인증 코드
     * @return 인증 성공 여부
     */
    boolean verifyEmail(String email, String code);

}