package com.mgnt.temp.domain.service;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.temp.domain.ConcertExceptionEnum;
import com.mgnt.temp.domain.entity.ConcertDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 콘서트 유효성 검사 클래스
 * <p>
 * 이 클래스는 콘서트와 관련된 유효성 검사를 처리합니다.
 */
@Component
@RequiredArgsConstructor
public class ConcertValidator {

    /**
     * 콘서트 날짜 리스트가 비어있는지 확인
     *
     * @param concertDateList 콘서트 날짜 리스트
     * @throws CustomException 날짜 리스트가 비어있는 경우 예외 발생
     */
    public void dateIsNull(List<ConcertDate> concertDateList) {
        if (concertDateList.isEmpty()) {
            throw new CustomException(ConcertExceptionEnum.DATE_IS_NULL, null, LogLevel.INFO);
        }
    }
}
