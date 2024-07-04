package com.mgnt.core.jwt;

import jakarta.servlet.http.HttpServletResponse;

public interface JwtInterface {

    /* token 생성 */
    String createToken(Long userId);

    /* token 추출 */
    String getToken();

    /* Header 로 token 전송 */
    void sendToken(HttpServletResponse response, String token);

    /* userId 추출 */
    Long getUserId() throws Exception;

    /* userId 검증 */
    boolean validToken(Long userId);
}

