package com.mgnt.ticketing.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mgnt.ticketing.entity.UserEntity;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenReqRes {
    private int statusCode;
    private String error;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String expirationTime;
    private String name;
    private String email;
    private String role;
    private String password;
    private UserEntity userEntity;
}
