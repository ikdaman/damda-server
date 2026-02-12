package com.damda.domain.auth.controller;

import com.damda.domain.auth.model.AuthReq;
import com.damda.domain.auth.model.AuthRes;
import com.damda.domain.auth.model.AuthSignupReq;
import com.damda.domain.auth.service.AuthService;
import com.damda.domain.auth.service.SocialAuthService;
import com.damda.global.auth.model.AuthMember;
import com.damda.global.auth.model.Tokens;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 인증 관련 API 컨트롤러
 * - 소셜 로그인, 회원가입, 토큰 재발급, 로그아웃 기능 제공
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SocialAuthService socialAuthService;
    private final AuthService authService;

    /**
     * 소셜 회원가입 (신규 회원)
     *
     * @param dto providerId, provider, nickname 정보
     * @param socialToken 소셜 플랫폼 토큰.
     *                     - 카카오/네이버: Access Token
     *                     - 구글/애플: ID Token
     * @return 헤더: Authorization (Access Token), refresh-token (Refresh Token)
     *         바디: 닉네임 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthRes> socialSignup(@Valid @RequestBody AuthSignupReq dto,
                                                @RequestHeader("social-token") String socialToken) {

        AuthRes res = socialAuthService.signup(dto, socialToken);

        return ResponseEntity.ok()
                .header("Authorization", res.getAccessToken())
                .header("refresh-token", res.getRefreshToken())
                .body(res);
    }

    /**
     * 소셜 로그인 (기존 회원)
     *
     * @param dto providerId와 provider 정보
     * @param socialToken 소셜 플랫폼 토큰
     *                    - 카카오/네이버: Access Token
     *                    - 구글/애플: ID Token
     * @return 헤더: Authorization (Access Token), refresh-token (Refresh Token)
     *         바디: 닉네임 정보
     */
    @PostMapping("/login")
    public ResponseEntity<AuthRes> socialLogin(@Valid @RequestBody AuthReq dto,
                                               @RequestHeader("social-token") String socialToken) {

        AuthRes res = socialAuthService.login(dto, socialToken);

        return ResponseEntity.ok()
                .header("Authorization", res.getAccessToken())
                .header("refresh-token", res.getRefreshToken())
                .body(res);
    }

    /**
     * Access Token 재발급
     *
     * @param accessToken 만료된 Access Token (Bearer 포함)
     * @param refreshToken 유효한 Refresh Token
     * @return 헤더: Authorization (새 Access Token), refresh-token (새 Refresh Token)
     */
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissueToken(@RequestHeader("Authorization") String accessToken,
                                             @RequestHeader("refresh-token") String refreshToken) {

        Tokens tokens = authService.reissueToken(accessToken, refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", tokens.getAccessToken());
        headers.add("refresh-token", tokens.getRefreshToken());

        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    /**
     * 로그아웃
     *
     * @param authMember Spring Security에서 인증된 회원 정보
     * @return 205 Reset Content
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthMember authMember) {
        authService.logout(authMember.getMember().getMemberId());
        return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
    }
}
