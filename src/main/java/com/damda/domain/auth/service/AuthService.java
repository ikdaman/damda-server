package com.damda.domain.auth.service;

import com.damda.global.auth.model.Tokens;
import com.damda.global.auth.token.AuthToken;
import com.damda.global.auth.token.AuthTokenProvider;
import com.damda.global.exception.BaseException;
import com.damda.global.util.RedisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.damda.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;

/**
 * 인증 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthTokenProvider authTokenProvider;
    private final RedisService redisService;

    @Value("${auth.refresh-token-validity}")
    private long refreshExpiry; // RefreshToken 만료일

    /**
     * Access Token 재발급
     */
    @Transactional
    public Tokens reissueToken(String accessToken, String refreshToken) {
        // 1. Refresh Token 검증 (validateRefreshToken 사용)
        AuthToken authRefreshToken = authTokenProvider.convertAuthToken(refreshToken);
        if (!authRefreshToken.validateRefreshToken()) throw new BaseException(INVALID_REFRESH_TOKEN);

        // 2. Refresh Token에서 memberId 추출
        String memberId = authRefreshToken.getTokenClaims().getSubject();

        // 3. Redis에 저장된 RefreshToken과 비교
        String storeRefreshToken = redisService.getValues(memberId)
                .orElseThrow(() -> new BaseException(INVALID_REFRESH_TOKEN));
        if(!storeRefreshToken.equals(refreshToken)) {
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }

        // 4. 신규 토큰 발급
        AuthToken newAccessToken = authTokenProvider.createUserAppToken(memberId);
        AuthToken newRefreshToken = authTokenProvider.createRefreshToken(memberId);

        Tokens tokens = Tokens.builder()
                .accessToken(newAccessToken.getToken())
                .refreshToken(newRefreshToken.getToken())
                .build();

        // 5. Redis에 새로운 Refresh Token 저장
        redisService.setValuesWithTimeout(memberId, newRefreshToken.getToken(), refreshExpiry);

        return tokens;
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(UUID memberId) {
        redisService.deleteValues(String.valueOf(memberId));
    }
}
