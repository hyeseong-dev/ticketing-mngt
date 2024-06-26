package com.mgnt.ticketing.base.filter;

import com.mgnt.ticketing.base.config.SecurityProperties;
import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.jwt.JwtUtil;
import com.mgnt.ticketing.base.jwt.UserDetailServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final UserDetailServiceImpl userDetailServiceImpl;
    private final SecurityProperties securityProperties;
    private final List<HandlerMapping> handlerMappings;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        log.debug("Request URI: {}", httpRequest.getRequestURI());

        if (!isExistingUri(httpRequest)) {
            log.debug("URI does not exist: {}", httpRequest.getRequestURI());
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, ErrorCode.ENDPOINT_NOT_FOUND.getMessage());
            return;
        }

        if (isAllowedUri(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        final String jwtToken = getJwtToken(httpRequest);
        if (jwtToken == null) {
            SecurityContextHolder.clearContext();
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, ErrorCode.ACCESS_DENIED.getMessage());
            return;
        }

        try {
            final String userEmail = jwtUtil.extractUsername(jwtToken);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(httpRequest, userEmail, jwtToken);
            }
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            log.error(e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.TOKEN_INVALID.getMessage());
            return;
        }

        chain.doFilter(request, response);
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
                    if (handler.getHandler() instanceof HandlerMethod) {
                        log.debug("Handler found for URI: {}", request.getRequestURI());
                        return true;
                    }
                }
            } catch (Exception e) {
                log.error("Error checking handler mapping for URI: {}", request.getRequestURI(), e);
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
