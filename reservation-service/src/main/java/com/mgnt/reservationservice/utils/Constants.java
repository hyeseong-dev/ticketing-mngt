package com.mgnt.reservationservice.utils;

public class Constants {
    public static final String WAITING_QUEUE_KEY = "queue:%d:%d";
    public static final int BATCH_SIZE = 10;
    public static final int ACCESS_TOKEN_EXPIRATION = 1 * 60 * 24; // 10 minutes
    public static final int ATTEMPT_COUNT_EXPIRATION = 24; // 24 hours
    public static final String ACCESS_TOKEN_KEY = "reservation:access:%d:%d:%d"; // userId:concertId:concertDateId
    public static final String ATTEMPT_COUNT_KEY = "reservation:attempts:%d:%d:%d";
}