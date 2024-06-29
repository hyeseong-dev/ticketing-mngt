package com.mgnt.ticketing.base.filter;

import com.mgnt.ticketing.base.jwt.JwtUtil;
import com.mgnt.ticketing.base.jwt.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 요청 헤더에서 액세스 토큰과 리프레시 토큰을 추출
        String accessTokenHeader = request.getHeader("Authorization");
        String refreshTokenHeader = request.getHeader("ReAuthorization");

        String accessToken = null;
        String email = null;

        // 액세스 토큰이 존재하고 "Bearer "로 시작하는 경우
        if (accessTokenHeader != null && accessTokenHeader.startsWith("Bearer ")) {
            accessToken = accessTokenHeader.substring(7); // "Bearer " 이후의 실제 토큰 값 추출
            try {
                email = jwtUtil.getEmailFromToken(accessToken); // 토큰에서 이메일 추출
            } catch (Exception e) {
                logger.error("Access Token 처리 중 오류 발생: ", e);
            }
        }

        // 이메일이 유효하고 현재 보안 컨텍스트에 인증 정보가 없는 경우
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email); // 이메일로 사용자 정보 로드
            if (jwtUtil.validateToken(accessToken)) { // 액세스 토큰 유효성 검사
                // 유효한 토큰인 경우 사용자 인증 정보 설정
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else if (refreshTokenHeader != null && refreshTokenHeader.startsWith("Bearer ")) {
                String refreshToken = refreshTokenHeader.substring(7); // "Bearer " 이후의 실제 리프레시 토큰 값 추출
                if (jwtUtil.validateToken(refreshToken)) { // 리프레시 토큰 유효성 검사
                    // 유효한 리프레시 토큰인 경우 새로운 액세스 토큰 생성
                    String newAccessToken = jwtUtil.createAccessToken(email);
                    response.setHeader("Authorization", "Bearer " + newAccessToken);

                    // 새로운 액세스 토큰으로 사용자 인증 정보 설정
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        // 다음 필터 체인으로 요청과 응답 전달
        chain.doFilter(request, response);
    }
}
