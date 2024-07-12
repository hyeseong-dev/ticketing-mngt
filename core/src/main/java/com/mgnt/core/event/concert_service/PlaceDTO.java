package com.mgnt.core.event.concert_service;

public record PlaceDTO(
        Long placeId,
        String name,
        int seatsCnt
) {
}