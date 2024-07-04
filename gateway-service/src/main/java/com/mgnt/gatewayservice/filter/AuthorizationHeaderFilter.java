package com.mgnt.gatewayservice.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.gatewayservice.exception.ErrorCode;
import com.mgnt.gatewayservice.exception.Response;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    private final SecretKey key;
    private final ObjectMapper mapper;

    public AuthorizationHeaderFilter(ObjectMapper objectMapper, @Value("${jwt.app.jwtSecretKey}") String secretKey) {
        super(Config.class);
        this.mapper = objectMapper;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public static class Config {
        // Put configuration properties here
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (!isTokenPresent(authorizationHeader)) {
                return onError(exchange, ErrorCode.INVALID_AUTHORIZATION);
            }

            String token = authorizationHeader.substring(7);

            if (!isJwtValid(token)) {
                return onError(exchange, ErrorCode.INVALID_JWT_TOKEN);
            }

            Long memberId = getClaimFromToken(token, "id", Long.class);
            String memberRole = getClaimFromToken(token, "role", String.class);
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("Member-Id", memberId.toString())
                    .header("Member-Role", memberRole)
                    .build();
            ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

            return chain.filter(modifiedExchange);
        };
    }

    private static boolean isTokenPresent(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    private <T> T getClaimFromToken(String token, String claimKey, Class<T> requiredType) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(claimKey, requiredType);
    }

    // Mono, Flux -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, ErrorCode err) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Response 클래스를 사용하여 에러 응답을 생성합니다.
        Response errorResponse = new Response(err.getStatus().name(), err.getMessage());

        // Response 객체를 JSON 형태로 변환하여 바디에 포함시킵니다.
        DataBuffer dataBuffer;
        try {
            byte[] responseBytes = mapper.writeValueAsBytes(errorResponse);
            dataBuffer = response.bufferFactory().wrap(responseBytes);
        } catch (JsonProcessingException e) {
            // JSON 변환에 실패한 경우 기본 오류 메시지를 사용합니다.
            dataBuffer = response.bufferFactory().wrap(e.getMessage().getBytes());
        }

        return response.writeWith(Mono.just(dataBuffer));
    }

    private boolean isJwtValid(String jwt) {
        try {
            String subject = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(jwt)
                    .getPayload()
                    .getSubject();
            return subject != null && !subject.isEmpty();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}