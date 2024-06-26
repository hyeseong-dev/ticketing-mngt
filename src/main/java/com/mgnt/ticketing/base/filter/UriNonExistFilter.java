package com.mgnt.ticketing.base.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.List;

@Slf4j
public class UriNonExistFilter extends OncePerRequestFilter {

    private final List<HandlerMapping> handlerMappings;

    public UriNonExistFilter(List<HandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        log.debug("Checking URI existence: {}", requestUri);
        if (!isExistingUri(request) || isExcludedPath(requestUri)) {
            log.debug("URI does not exist: {}", requestUri);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isExistingUri(HttpServletRequest request) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            try {
                HandlerExecutionChain handler = handlerMapping.getHandler(request);
                if (handler != null) {
                    log.debug("Handler found for URI: {}", request.getRequestURI());
                    return true;
                }
            } catch (Exception e) {
                log.error("Error checking handler mapping for URI: {}", request.getRequestURI(), e);
            }
        }
        log.debug("No handler found for URI: {}", request.getRequestURI());
        return false;
    }

    // 특정 경로를 필터링에서 제외하는 메서드
    private boolean isExcludedPath(String requestUri) {
        return false;
//        return requestUri.startsWith("/swagger-ui/")
//                || requestUri.startsWith("/v3/api-docs/")
//                || requestUri.startsWith("/webjars/");
    }
}
