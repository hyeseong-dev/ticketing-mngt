package com.mgnt.concertservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        List<Seat> allSeats = seatRepository.findAll();

        for (Seat seat : allSeats) {
            String jsonSeat = objectMapper.writeValueAsString(seat);
            redisTemplate.opsForZSet().add("all_seats", jsonSeat, seat.getSeatId());
        }
    }
}
