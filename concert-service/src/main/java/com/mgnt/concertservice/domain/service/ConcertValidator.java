package com.mgnt.concertservice.domain.service;

import com.mgnt.concertservice.domain.entity.ConcertDate;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
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
            throw new CustomException(ErrorCode.DATE_IS_NULL, null, Level.ERROR);
        }
    }
}
