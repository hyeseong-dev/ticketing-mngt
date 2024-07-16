package com.mgnt.reservationservice.controller;

import com.mgnt.core.event.reservation_service.QueueEntryRequest;
import com.mgnt.core.event.reservation_service.QueueEntryResponse;
import com.mgnt.core.event.reservation_service.QueueStatusResponse;
import com.mgnt.core.exception.ApiResult;
import com.mgnt.reservationservice.domain.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/queue")
@RestController
@RequiredArgsConstructor
public class QueueController {

    private final QueueService service;


    @PostMapping()
    public ApiResult<QueueEntryResponse> reserveQueue(
            @RequestHeader("User-Id") Long userId,
            @RequestBody QueueEntryRequest request
    ) {
        return ApiResult.success(service.enterQueue(userId, request));
    }

    @GetMapping("/status")
    public ApiResult<QueueStatusResponse> getQueueStatus(
            @RequestHeader("User-Id") Long userId,
            @RequestBody QueueEntryRequest request
    ) {

        return ApiResult.success(service.getQueueStatus(userId, request));
    }

    @DeleteMapping()
    public ApiResult<Void> removeFromQueue(
            @RequestHeader("User-Id") Long userId,
            @RequestBody QueueEntryRequest request
    ) {
        service.removeFromQueue(userId, request);
        return ApiResult.success(null);
    }

}

