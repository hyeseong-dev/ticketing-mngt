package com.mgnt.ticketing.base.jwt;

import com.mgnt.ticketing.base.config.SecurityProperties;
import com.mgnt.ticketing.base.error.ErrorCode;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.HandlerExecutionChain;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailServiceImpl userDetailServiceImpl;
    private final SecurityProperties securityProperties;

    @Autowired
    private List<HandlerMapping> handlerMappings;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("Request URI: {}", request.getRequestURI());

        if (!isExistingUri(request)) {
            log.debug("URI does not exist: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ErrorCode.ENDPOINT_NOT_FOUND.getMessage());
            return;
        }

        if (isAllowedUri(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = getJwtToken(request);
        if (jwtToken == null) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ErrorCode.ACCESS_DENIED.getMessage());
            return;
        }

        try {
            final String userEmail = jwtUtil.extractUsername(jwtToken);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(request, userEmail, jwtToken);
            }
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.TOKEN_INVALID.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedUri(String requestUri) {
        List<String> allowedUris = securityProperties.getAllowedUris();
        boolean isAllowed = allowedUris.stream().anyMatch(requestUri::startsWith);
        log.debug("Request URI: {}, isAllowed: {}", requestUri, isAllowed);
        return isAllowed;
    }

    private boolean isExistingUri(HttpServletRequest request) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            try {
                HandlerExecutionChain handler = handlerMapping.getHandler(request);
                if (handler != null) {
                    // 이 부분에서 실제 핸들러가 존재하는지 확인하는 로직 추가
                    if (handler.getHandler() instanceof HandlerMethod) {
                        log.debug("Handler found for URI: {}", request.getRequestURI());
                        return true;
                    }
                }
            } catch (Exception e) {
                // Ignore and continue checking other handler mappings
            }
        }
        log.debug("No handler found for URI: {}", request.getRequestURI());
        return false;
    }

    private String getJwtToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
            return authHeader.substring(JwtUtil.BEARER_PREFIX.length());
        }
        return null;
    }

    private void authenticateUser(HttpServletRequest request, String userEmail, String jwtToken) {
        UserDetails userDetails = userDetailServiceImpl.loadUserByUsername(userEmail);
        if (jwtUtil.isTokenValid(jwtToken, userDetails)) {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(token);
        }
    }
}
