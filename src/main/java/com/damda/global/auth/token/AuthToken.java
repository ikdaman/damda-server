package com.damda.global.auth.token;

import com.damda.global.auth.enumerate.RoleType;
import com.damda.global.exception.BaseException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static com.damda.global.exception.ErrorCode.*;

@Slf4j
@RequiredArgsConstructor
public class AuthToken {

    @Getter
    private final String token;
    private final Key key;

    private static final String AUTHORITIES_KEY = "role";

    AuthToken(String id, RoleType roleType, Date expiry, Key key, String tokenType) {
        String role = roleType.toString(); // USER, ADMIN
        this.key = key;
        this.token = createAuthToken(id, role, expiry, tokenType);
    }

    // AccessToken(appToken) 생성
    private String createAuthToken(String id, String role, Date expiry, String tokenType) {
        return Jwts.builder()
                .setSubject(id)
                .claim(AUTHORITIES_KEY, role)
                .claim("type", tokenType)           // 토큰 타입
                .setId(UUID.randomUUID().toString())   // 고유 ID
                .setIssuedAt(new Date())               // 발급 시간
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(expiry)
                .compact();
    }

    /**
     * Access Token(app token) 검증
     */
    public boolean validate() {
        return this.getTokenClaims() != null;
    }

    /**
     * Refresh Token 검증
     */
    public boolean validateRefreshToken() {
        return this.getRefreshTokenClaims() != null;
    }

    /**
     * Access Token Claims 추출
     */
    public Claims getTokenClaims() {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody(); // token의 Body가 다음의 exception들로 인해 유효하지 않으면 각각의 로그를 콘솔에 출력

        } catch (SignatureException e) {
            throw new BaseException(INVALID_ACCESS_TOKEN_SIGNATURE);
        } catch (MalformedJwtException e) {
            // 처음 로그인(/auth/kakao) 할 때, AccessToken(여기선 appToken) 없이 접근해도 token validate 체크
            // -> exception 터트리지 않고 catch로 잡아줌
            throw new BaseException(INVALID_ACCESS_TOKEN_FORMAT);
        } catch (ExpiredJwtException e) {
            throw new BaseException(EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new BaseException(UNSUPPORTED_ACCESS_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_ACCESS_TOKEN);
        } catch (Exception e) {
            log.error("Access Token 검증 중 예외 발생: {}", e.getClass().getSimpleName(), e);
            throw new BaseException(INVALID_ACCESS_TOKEN);
        }
    }

    /**
     * Refresh Token Claims 추출 (만료 시 INVALID_REFRESH_TOKEN)
     */
    public Claims getRefreshTokenClaims() {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (SignatureException e) {
            throw new BaseException(INVALID_REFRESH_TOKEN_SIGNATURE);
        } catch (MalformedJwtException e) {
            throw new BaseException(INVALID_REFRESH_TOKEN_FORMAT);
        } catch (ExpiredJwtException e) {   // Refresh Token 만료
            throw new BaseException(EXPIRED_REFRESH_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new BaseException(UNSUPPORTED_REFRESH_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_REFRESH_TOKEN);
        } catch (Exception e) {
            log.error("Refresh Token 검증 중 예외 발생: {}", e.getClass().getSimpleName(), e);
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }
    }
}