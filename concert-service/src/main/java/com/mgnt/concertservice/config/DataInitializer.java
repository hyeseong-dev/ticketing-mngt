package com.mgnt.concertservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.concertservice.domain.entity.Inventory;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.InventoryRepository;
import com.mgnt.concertservice.domain.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mgnt.core.constants.Constants.ALL_INVENTORY_KEY;
import static com.mgnt.core.constants.Constants.ALL_SEATS_KEY;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SeatRepository seatRepository;
    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        initializeSeats();
        initializeInventory();
    }

    private void initializeSeats() throws Exception {
        // 기존 데이터 삭제
        redisTemplate.delete(ALL_SEATS_KEY);
        List<Seat> allSeats = seatRepository.findAll();

        for (Seat seat : allSeats) {
            String jsonSeat = objectMapper.writeValueAsString(seat);
            redisTemplate.opsForHash().put(ALL_SEATS_KEY, seat.getSeatId().toString(), jsonSeat);
        }

        long seatCount = redisTemplate.opsForHash().size(ALL_SEATS_KEY);
        System.out.println("Loaded " + seatCount + " seat records into Redis.");
    }

    private void initializeInventory() throws Exception {
        redisTemplate.delete(ALL_INVENTORY_KEY);
        List<Inventory> allInventory = inventoryRepository.findAll();

        Map<String, String> inventoryMap = allInventory.stream()
                .collect(Collectors.toMap(
                        inventory -> String.format("%d:%d", inventory.getConcertId(), inventory.getConcertDateId()),
                        this::convertToJson
                ));

        redisTemplate.opsForHash().putAll(ALL_INVENTORY_KEY, inventoryMap);

        long inventoryCount = redisTemplate.opsForHash().size(ALL_INVENTORY_KEY);
        System.out.println("Loaded " + inventoryCount + " inventory records into Redis.");
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }
}