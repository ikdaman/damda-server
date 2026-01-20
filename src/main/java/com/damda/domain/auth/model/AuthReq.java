package com.damda.domain.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthReq {
    @NotBlank(message = "소셜 제공자는 필수입니다. (NAVER, KAKAO, GOOGLE, APPLE)")
    private String provider; // 인증업체

    @NotBlank(message = "Provider ID는 필수입니다. (access-token: 카카오, 네이버 | id-token: 구글, 애플)")
    private String providerId;

    @Builder
    public AuthReq(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }
}
