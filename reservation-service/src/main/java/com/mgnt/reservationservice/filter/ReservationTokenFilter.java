package com.mgnt.reservationservice.filter;

import com.mgnt.reservationservice.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ReservationTokenFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CustomHttpServletRequestWrapper wrappedRequest = new CustomHttpServletRequestWrapper(request);

        String reservationToken = request.getHeader("X-Reservation-Token");
        String userIdHeader = request.getHeader("User-Id");

        // Preserve the User-Id header from API Gateway if present
        if (userIdHeader != null) {
            wrappedRequest.addHeader("User-Id", userIdHeader);
        }

        // Process reservation token if present
        if (reservationToken != null && reservationToken.startsWith("Bearer ")) {
            String token = reservationToken.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    addTokenInfoToHeaders(wrappedRequest, token, userIdHeader);
                } else {
                    log.warn("Invalid reservation token");
                    // Continue processing instead of returning unauthorized
                }
            } catch (Exception e) {
                log.error("Error parsing reservation token", e);
                // Continue processing instead of returning unauthorized
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(wrappedRequest, response);
    }

    private void addTokenInfoToHeaders(CustomHttpServletRequestWrapper wrappedRequest, String token, String userIdHeader) {
        Long concertId = jwtUtil.getConcertIdFromToken(token);
        Long concertDateId = jwtUtil.getConcertDateIdFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);

        if (concertId != null) {
            wrappedRequest.addHeader("X-Concert-Id", concertId.toString());
        }
        if (concertDateId != null) {
            wrappedRequest.addHeader("X-Concert-Date-Id", concertDateId.toString());
        }
        if (userId != null) {
            wrappedRequest.addHeader("X-User-Id", userId.toString());
        }
        wrappedRequest.addHeader("X-Reservation-Token", token);

        // Only add X-User-Id if it's not already present from API Gateway
        if (userIdHeader == null && userId != null) {
            wrappedRequest.addHeader("X-User-Id", userId.toString());
        }

        log.info("Added reservation info to headers: UserId={}, ConcertId={}, ConcertDateId={}",
                userId, concertId, concertDateId);
    }
}