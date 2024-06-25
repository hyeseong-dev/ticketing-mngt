package com.mgnt.ticketing.base.jwt;

import com.mgnt.ticketing.base.config.SecurityProperties;
import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.error.exceptions.CustomAccessDeniedException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailServiceImpl userDetailServiceImpl;
    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 허용된 URI는 인증 기능 로직을 받지 않도록 함.
        if (isAllowedUri(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = getJwtToken(request);
        if(jwtToken == null) {
            SecurityContextHolder.clearContext();  // 인증 정보 제거
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
            request.setAttribute("exception", e);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedUri(String requestUri) {
        List<String> allowedUris = securityProperties.getAllowedUris();
        boolean isAllowed = allowedUris.stream().anyMatch(requestUri::startsWith);
        log.debug("Request URI: {}, isAllowed: {}", requestUri, isAllowed);
        return isAllowed;
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