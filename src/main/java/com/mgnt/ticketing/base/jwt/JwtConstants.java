package com.mgnt.ticketing.base.jwt;

import org.springframework.beans.factory.annotation.Value;

public final class JwtConstants {

    @Value("${jwt.secret}")
    public static String SECRET_KEY;

    public final static long EXP_TIME = 60 * 60 * 24 * 7;
}
