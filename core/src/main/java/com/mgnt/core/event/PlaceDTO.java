package com.mgnt.core.event;

public record PlaceDTO(
        Long placeId,
        String name,
        int seatsCnt
) {
}