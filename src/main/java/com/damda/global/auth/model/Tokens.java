package com.damda.global.auth.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tokens {
    private String accessToken;
    private String refreshToken;

    @Builder
    public Tokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
