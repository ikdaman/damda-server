package com.damda.domain.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthRes {

    @JsonIgnore
    private String accessToken;    // App의 AccessToken

    @JsonIgnore
    private String refreshToken;   // RefreshToken

    private String nickname;

    @Builder
    public AuthRes(String accessToken, String refreshToken, String nickname) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.nickname = nickname;
    }
}
