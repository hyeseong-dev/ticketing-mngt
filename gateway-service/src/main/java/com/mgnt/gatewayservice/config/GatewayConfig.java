package com.mgnt.gatewayservice.config;

import com.mgnt.gatewayservice.filter.AuthorizationHeaderFilter;
import com.mgnt.gatewayservice.filter.ExcludeUriFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class GatewayConfig {

    private final ExcludeUriFilter excludeUriFilter;
    private final AuthorizationHeaderFilter authFilter;

    public GatewayConfig(ExcludeUriFilter excludeUriFilter, AuthorizationHeaderFilter authFilter) {
        this.excludeUriFilter = excludeUriFilter;
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        // TODO /api/auth/** 회원가입, 로그인, verify-email
        return builder.routes()
                .route("user-service-auth-public", r -> r
                        .path("/api/auth/**")
                        .uri("lb://USER-SERVICE"))
                .route("user-service-auth-protected", r -> r
                        .path("/api/auth/logout")
                        .filters(f -> f.filter(authFilter.apply(new AuthorizationHeaderFilter.Config())))
                        .uri("lb://USER-SERVICE"))
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthorizationHeaderFilter.Config())))
                        .uri("lb://USER-SERVICE"))
                .route("reservation-service", r -> r
                        .path("/api/reservation/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthorizationHeaderFilter.Config())))
                        .uri("lb://RESERVATION-SERVICE"))
                .route("payment-service", r -> r
                        .path("/api/payment/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthorizationHeaderFilter.Config())))
                        .uri("lb://PAYMENT-SERVICE"))
                .build();


    }
}