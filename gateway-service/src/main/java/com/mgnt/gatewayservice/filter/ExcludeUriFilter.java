package com.mgnt.gatewayservice.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ExcludeUriFilter extends AbstractGatewayFilterFactory<ExcludeUriFilter.Config> {
    private static final List<String> EXCLUDE_URI_LIST = Arrays.asList(
            "/user-service/api/auth/signup",
            "/user-service/api/auth/login",
            "/user-service/api/auth/verify-email"
    );

    public ExcludeUriFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String requestUri = exchange.getRequest().getURI().getPath();
            if (EXCLUDE_URI_LIST.stream().anyMatch(requestUri::startsWith)) {
                log.debug("Exclude URI: {}", requestUri);
                return chain.filter(exchange);
            } else {
                return chain.filter(exchange);
            }
        };
    }

    @Getter
    @Setter
    public static class Config {
        // 필요한 경우 추가 설정 정의
    }
}