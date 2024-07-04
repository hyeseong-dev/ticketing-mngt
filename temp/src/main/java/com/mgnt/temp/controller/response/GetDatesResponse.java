package com.mgnt.temp.controller.response;

import com.mgnt.temp.domain.entity.ConcertDate;

import java.time.ZonedDateTime;
import java.util.List;

public record GetDatesResponse(
        List<DateInfo> dates
) {

    public record DateInfo(
            Long concertDateId,
            ZonedDateTime date,
            boolean available
    ) {
        public static DateInfo from(ConcertDate concertDate, boolean available) {
            return new DateInfo(concertDate.getConcertDateId(), concertDate.getConcertDate(), available);
        }
    }
}
