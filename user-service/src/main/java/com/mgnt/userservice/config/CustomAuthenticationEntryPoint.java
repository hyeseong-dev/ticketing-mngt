package com.mgnt.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        if (authException.getClass().equals(BadCredentialsException.class)) {
            setResponse(response, ErrorCode.INVALID_CREDENTIALS, authException);
        }

    }

    private void setResponse(HttpServletResponse response, ErrorCode errorCode, AuthenticationException ex) throws IOException {
        log.error("error message {}", errorCode.getMessage(), ex);
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ApiResult<String> fail = ApiResult.error(errorCode.getCode(), errorCode.getMessage(), errorCode.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(fail));
    }
}