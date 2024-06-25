package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.dto.GetSeatsQueryResDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertQueryDslRepository {

    List<GetSeatsQueryResDto> getSeatsByConcertDate(Long concertId, Long concertDateId);
}