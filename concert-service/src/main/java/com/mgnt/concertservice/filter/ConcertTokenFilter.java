package com.mgnt.concertservice.filter;

import com.mgnt.concertservice.utils.JwtUtil;
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
public class ConcertTokenFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String reservationToken = request.getHeader("X-Reservation-Token");
        String userIdHeader = request.getHeader("User-Id");

        CustomHttpServletRequestWrapper wrappedRequest = new CustomHttpServletRequestWrapper(request);

        // Preserve the User-Id header from API Gateway
        if (userIdHeader != null) {
            wrappedRequest.addHeader("User-Id", userIdHeader);
        }

        if (reservationToken != null && reservationToken.startsWith("Bearer ")) {
            String token = reservationToken.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    Long concertId = jwtUtil.getConcertIdFromToken(token);
                    Long concertDateId = jwtUtil.getConcertDateIdFromToken(token);
                    Long userId = jwtUtil.getUserIdFromToken(token);

                    if (concertId != null && concertDateId != null && userId != null) {
                        wrappedRequest.addHeader("X-Concert-Id", concertId.toString());
                        wrappedRequest.addHeader("X-Concert-Date-Id", concertDateId.toString());
                        wrappedRequest.addHeader("X-User-Id", userId.toString());
                        wrappedRequest.addHeader("X-Reservation-Token", token);

                        // Only add X-User-Id if it's not already present from API Gateway
                        if (userIdHeader == null) {
                            wrappedRequest.addHeader("X-User-Id", userId.toString());
                        }
                        log.info("Added reservation info to headers: UserId={}, ConcertId={}, ConcertDateId={} X-User-Id={}", userId, concertId, concertDateId, userId,
                                userIdHeader, concertId, concertDateId, userId);
                    }
                } else {
                    log.warn("Invalid reservation token");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                log.error("Error parsing reservation token", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(wrappedRequest, response);
    }
}