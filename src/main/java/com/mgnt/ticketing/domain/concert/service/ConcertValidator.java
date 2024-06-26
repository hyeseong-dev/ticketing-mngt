package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.domain.concert.ConcertCustomException;
import com.mgnt.ticketing.domain.concert.ConcertExceptionEnum;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConcertValidator {

    public void dateIsNull(List<ConcertDate> concertDateList) {
        if (concertDateList.isEmpty()) {
            throw new ConcertCustomException(ConcertExceptionEnum.DATE_IS_NULL);
        }
    }
}
