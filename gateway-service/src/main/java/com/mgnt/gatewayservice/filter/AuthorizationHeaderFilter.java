package com.mgnt.gatewayservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.gatewayservice.exception.ErrorCode;
import com.mgnt.gatewayservice.exception.Response;
import com.mgnt.gatewayservice.utils.JwtUtil;
import com.mgnt.gatewayservice.utils.RefreshTokenResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    public AuthorizationHeaderFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.webClientBuilder = webClientBuilder;
    }

    public static class Config {
        // 필요한 경우 설정 속성을 여기에 추가
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String refreshTokenHeader = request.getHeaders().getFirst("X-Refresh-Token");

            if (!isTokenPresent(authHeader)) {
                return onError(exchange, ErrorCode.INVALID_AUTHORIZATION);
            }

            String token = authHeader.substring(7);

            if (jwtUtil.isBlacklisted(token)) {
                return onError(exchange, ErrorCode.INVALID_JWT_TOKEN);
            }

            if (jwtUtil.isTokenExpired(token)) {
                if (refreshTokenHeader == null) {
                    return onError(exchange, ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                }
                return handleExpiredToken(exchange, refreshTokenHeader, token, chain);
            }

            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, ErrorCode.INVALID_JWT_TOKEN);
            }

            addAuthorizationHeaders(exchange, token);
            return chain.filter(exchange);
        };
    }

    private boolean isTokenPresent(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    private Mono<Void> handleExpiredToken(ServerWebExchange exchange, String refreshToken, String accessToken, GatewayFilterChain chain) {
        Long userId = jwtUtil.getClaimFromToken(refreshToken, "id", Long.class);
        String userRole = jwtUtil.getClaimFromToken(refreshToken, "role", String.class);
        Long remainingTimeInMillis = jwtUtil.getRemainingTime(accessToken);

        return webClientBuilder.build().post()
                .uri("http://user-service/api/auth/refresh")
                .header("User-Id", userId.toString())
                .header("User-Role", userRole)
                .header("Blacklist-Token", accessToken)
                .header("Remaining-time-in-millis", remainingTimeInMillis.toString())
                .retrieve()
                .bodyToMono(RefreshTokenResponseDto.class)
                .flatMap(response -> {
                    if (response.userId() != null) {
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.accessToken())
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    } else {
                        return onError(exchange, ErrorCode.INVALID_REFRESH_TOKEN);
                    }
                })
                .onErrorResume(e -> onError(exchange, ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private void addAuthorizationHeaders(ServerWebExchange exchange, String token) {
        Long userId = jwtUtil.getClaimFromToken(token, "id", Long.class);
        String userRole = jwtUtil.getClaimFromToken(token, "role", String.class);

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("User-Id", userId.toString())
                .header("User-Role", userRole)
                .build();
        exchange.mutate().request(request).build();
    }

    private Mono<Void> onError(ServerWebExchange exchange, ErrorCode errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Response errorResponse = new Response(errorCode.getStatus().name(), errorCode.getMessage());
        byte[] responseBytes;
        try {
            responseBytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception e) {
            responseBytes = "{'error': 'Internal Server Error'}".getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
        return response.writeWith(Mono.just(buffer));
    }
}