package com.mgnt.core.constants;

public class Constants {
    public static final String WAITING_QUEUE_KEY = "queue:%d:%d";
    public static final int BATCH_SIZE = 10;
    public static final int ACCESS_TOKEN_EXPIRATION = 1 * 60 * 24; // 10 minutes
    public static final int ATTEMPT_COUNT_EXPIRATION = 24; // 24 hours
    public static final String ACCESS_TOKEN_KEY = "reservation:access:%d:%d:%d"; // userId:concertId:concertDateId
    public static final String ATTEMPT_COUNT_KEY = "reservation:attempts:%d:%d:%d";
    public static final String ALL_RESERVATION_KEY = "all_reservations";
    public static final String ALL_SEATS_KEY = "all_seats";
    public static final String ALL_INVENTORY_KEY = "all_inventory";
    public static final String RESERVATION_INCR_KEY = "reservation:next_id";
    public static final long CACHE_TTL_SECONDS = 3600; // 1시간

    // 새로 추가할 상수들
    public static final String SEAT_KEY_PREFIX = "seat:";
    public static final String TEMP_RESERVATION_KEY = "temp_reservation:%d"; // concertDateId:seatId
    public static final String EXPIRY_KEY = "expiry:seat:%d"; // concertDateId:seatId
    public static final long TEMP_RESERVATION_MINUTES = 5L;
    public static final long TEMP_RESERVATION_SECONDS = TEMP_RESERVATION_MINUTES * 60;

    // Kafka 토픽 이름
    public static final String TOPIC_RESERVATION_REQUESTS = "reservation-requests";
    public static final String TOPIC_SEAT_STATUS_UPDATES = "seat-status-updates";
    private static final String TOPIC_RESERVATIONS_CREATED = "reservations-created";
    public static final String TOPIC_RESERVATION_FAILED = "reservation-failed";
    private static final String TOPIC_PAYMENT_COMPLETED = "payment-completed";
    public static final String TOPIC_RESERVATION_CONFIRMED = "reservation-confirmed";
    public static final String TOPIC_INVENTORY_RESERVATION_RESPONSES = "inventory-reservation-responses";
    public static final String TOPIC_SEAT_RESERVATION_REQUESTS = "seat-reservation-requests";
    public static final String TOPIC_SEAT_RESERVATION_RESPONSES = "seat-reservation-responses";
    public static final String TOPIC_CONCERT_INFO_REQUESTS = "concert-info-requests";
    public static final String TOPIC_CONCERT_INFO_RESPONSES = "concert-info-responses";
    public static final String TOPIC_INVENTORY_RESERVATION_REQUESTS = "inventory-reservation-requests";
}
