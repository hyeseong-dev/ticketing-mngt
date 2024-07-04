package com.mgnt.gatewayservice.config;

import com.mgnt.gatewayservice.filter.AuthorizationHeaderFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthorizationHeaderFilter authFilter;

    public GatewayConfig(AuthorizationHeaderFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.stripPrefix(2).filter(authFilter.apply(new AuthorizationHeaderFilter.Config())))
                        .uri("lb://USER-SERVICE"))
                .route("reservation-service", r -> r
                        .path("/api/reservation/**")
                        .filters(f -> f.stripPrefix(2).filter(authFilter.apply(new AuthorizationHeaderFilter.Config())))
                        .uri("lb://RESERVATION-SERVICE"))
                .route("payment-service", r -> r
                        .path("/api/payment/**")
                        .filters(f -> f.stripPrefix(2).filter(authFilter.apply(new AuthorizationHeaderFilter.Config())))
                        .uri("lb://PAYMENT-SERVICE"))
                .build();
    }
}