package com.mgnt.common.jwt;

import org.springframework.beans.factory.annotation.Value;

public final class JwtConstants {

    @Value("${jwt.secret}")
    public static String SECRET_KEY;
    // 5시간(5 * 60 * 60 * 1000 밀리초)
    public final static long EXP_TIME = 5 * 60 * 60 * 1000;
}
