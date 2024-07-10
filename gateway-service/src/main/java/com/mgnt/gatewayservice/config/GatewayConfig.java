package com.mgnt.gatewayservice.config;

import com.mgnt.gatewayservice.filter.AuthorizationHeaderFilter;
import com.mgnt.gatewayservice.filter.RequestBodyFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class GatewayConfig {

    private final AuthorizationHeaderFilter authFilter;
    private final RequestBodyFilter requestBodyFilter;

    public GatewayConfig(AuthorizationHeaderFilter authFilter, RequestBodyFilter requestBodyFilter) {
        this.authFilter = authFilter;
        this.requestBodyFilter = requestBodyFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service-auth-protected", r -> r
                        .path("/api/auth/logout")
                        .filters(f -> f.filter(authFilter.apply(new AuthorizationHeaderFilter.Config()), Ordered.HIGHEST_PRECEDENCE)
                                .filter(requestBodyFilter.apply(new RequestBodyFilter.Config()), Ordered.HIGHEST_PRECEDENCE + 1))
                        .uri("lb://USER-SERVICE"))
                .route("user-service-auth-public", r -> r
                        .path("/api/auth/**")
                        .uri("lb://USER-SERVICE"))
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthorizationHeaderFilter.Config()), Ordered.HIGHEST_PRECEDENCE)
                                .filter(requestBodyFilter.apply(new RequestBodyFilter.Config()), Ordered.HIGHEST_PRECEDENCE + 1))
                        .uri("lb://USER-SERVICE"))
                .route("reservation-service", r -> r
                        .path("/api/reservations/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthorizationHeaderFilter.Config()), Ordered.HIGHEST_PRECEDENCE)
                                .filter(requestBodyFilter.apply(new RequestBodyFilter.Config()), Ordered.HIGHEST_PRECEDENCE + 1))
                        .uri("lb://RESERVATION-SERVICE"))
                .route("payment-service", r -> r
                        .path("/api/payment/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthorizationHeaderFilter.Config()), Ordered.HIGHEST_PRECEDENCE)
                                .filter(requestBodyFilter.apply(new RequestBodyFilter.Config()), Ordered.HIGHEST_PRECEDENCE + 1))
                        .uri("lb://PAYMENT-SERVICE"))
                .route("concert-service", r -> r
                        .path("/api/concerts/**")
                        .uri("lb://CONCERT-SERVICE"))
                .build();
    }
}
