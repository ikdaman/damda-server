package com.damda.domain.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 회원가입 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthSignupReq {
    @NotBlank(message = "소셜 제공자는 필수입니다. (NAVER, KAKAO, GOOGLE, APPLE)")
    private String provider; // 인증업체

    @NotBlank(message = "Provider ID는 필수입니다. (access-token: 카카오, 네이버 | id-token: 구글, 애플)")
    private String providerId;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 10, message = "닉네임은 최대 10자입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9 ]+$",
            message = "닉네임은 한글/영문/숫자/공백만 사용 가능합니다.")
    private String nickname;

    @Builder
    public AuthSignupReq(String provider, String providerId, String nickname) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
    }
}
