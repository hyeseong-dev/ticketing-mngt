package com.mgnt.ticketing.base.enums;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 메시지 공통 인터페이스
 *
 * 이 인터페이스는 메시지와 관련된 코드를 제공하는 역할을 합니다.
 * Jackson 라이브러리를 사용하여 JSON 직렬화를 지원합니다.
 */
@JsonSerialize(using = MessageCommSerializer.class)
public interface MessageCommInterface {
    /**
     * 메시지 코드 반환
     *
     * @return 메시지 코드
     */
    String getCode();

    /**
     * 메시지 반환
     *
     * @return 메시지
     */
    String getMessage();
}
