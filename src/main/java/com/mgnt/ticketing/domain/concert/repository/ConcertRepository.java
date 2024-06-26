package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.dto.GetSeatsQueryResDto;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ConcertRepository {

    List<Concert> findAll();

    Concert findById(Long concertId);

}