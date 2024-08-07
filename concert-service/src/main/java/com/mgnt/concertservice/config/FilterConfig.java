package com.mgnt.concertservice.config;

import com.mgnt.concertservice.filter.ConcertTokenFilter;
import com.mgnt.concertservice.utils.JwtUtil;
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
    public FilterRegistrationBean<ConcertTokenFilter> reservationTokenFilter() {
        FilterRegistrationBean<ConcertTokenFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new ConcertTokenFilter(jwtUtil));
        registrationBean.addUrlPatterns("/api/concerts/*"); // 필터를 적용할 URL 패턴 설정
        registrationBean.setOrder(1); // 필터 체인에서의 순서 설정

        return registrationBean;
    }
}