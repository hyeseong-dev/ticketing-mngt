package com.mgnt.reservationservice.config;

import com.mgnt.reservationservice.filter.ReservationTokenFilter;
import com.mgnt.reservationservice.utils.JwtUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final JwtUtil jwtUtil;

    public FilterConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public FilterRegistrationBean<ReservationTokenFilter> reservationTokenFilter() {
        FilterRegistrationBean<ReservationTokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ReservationTokenFilter(jwtUtil));
        registrationBean.addUrlPatterns("/api/reservations/*"); // URL 패턴 점검
        registrationBean.setOrder(1); // 필터 체인에서의 순서 설정
        return registrationBean;
    }
}