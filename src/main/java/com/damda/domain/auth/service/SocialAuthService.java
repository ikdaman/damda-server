package com.damda.domain.auth.service;

import com.damda.domain.auth.model.AuthReq;
import com.damda.domain.auth.model.AuthRes;
import com.damda.domain.auth.model.AuthSignupReq;
import com.damda.domain.member.entity.Member;
import com.damda.domain.member.repository.MemberRepository;
import com.damda.domain.member.service.MemberService;
import com.damda.global.auth.enumerate.Provider;
import com.damda.global.auth.token.AuthToken;
import com.damda.global.auth.token.AuthTokenProvider;
import com.damda.global.exception.BaseException;
import com.damda.global.util.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.damda.global.exception.ErrorCode.*;

/**
 * 통합 소셜 로그인/회원가입 서비스
 */
@Service
@RequiredArgsConstructor
public class SocialAuthService implements SocialService {

    private final SocialTokenValidator tokenValidator;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final AuthTokenProvider authTokenProvider;
    private final RedisService redisService;

    @Value("${auth.refresh-token-validity}")
    private long refreshExpiry; // RefreshToken 만료일

    /**
     * 소셜 회원가입 (신규 회원)
     */
    @Override
    @Transactional
    public AuthRes signup(AuthSignupReq req, String socialToken) {
        // 1. Provider 문자열 → Enum 변환
        Provider provider = Provider.from(req.getProvider());

        // 2. 토큰 검증 및 providerId 추출
        String providerIdFromToken = tokenValidator.validate(provider, socialToken);

        // 3. 요청의 providerId와 토큰에서 추출한 providerId 일치 검증
        if (!req.getProviderId().equals(providerIdFromToken)) {
            throw new BaseException(NOT_MATCH_TOKEN_PROVIDER);
        }

        // 4. 이미 가입된 회원인지 체크
        if (memberRepository.findByProviderAndProviderIdAndStatus(
                Member.Provider.valueOf(provider.name()),
                providerIdFromToken,
                Member.Status.ACTIVE
        ).isPresent()) {
            throw new BaseException(MEMBER_ALREADY_EXISTS);
        }

        // 5. 닉네임 중복 체크
        if (!memberService.isNicknameAvailable(req.getNickname())) {
            throw new BaseException(CONFLICT_NICKNAME);
        }

        // 6. 신규 회원 저장
        Member newMember = Member.builder()
                .provider(Member.Provider.valueOf(provider.name()))
                .providerId(providerIdFromToken)
                .nickname(req.getNickname())
                .build();
        Member savedMember = memberRepository.save(newMember);

        // 7. 토큰 발급 및 Redis 저장
        return issueTokens(savedMember);
    }

    /**
     * 소셜 로그인 (기존 회원만)
     */
    @Override
    @Transactional
    public AuthRes login(AuthReq req, String socialToken) {

        // 1. Provider 문자열 → Enum 변환
        Provider provider = Provider.from(req.getProvider());

        // 2. 토큰 검증 및 providerId(sub) 추출
        String providerIdFromToken = tokenValidator.validate(provider, socialToken);

        // 3. 요청의 providerId와 토큰에서 추출한 providerId 일치 검증
        if (!req.getProviderId().equals(providerIdFromToken)) throw new BaseException(NOT_MATCH_TOKEN_PROVIDER);

        // 4. 회원 조회 (없으면 404 에러)
        Member member = memberRepository
                .findByProviderAndProviderIdAndStatus(
                        Member.Provider.valueOf(provider.name()),
                        providerIdFromToken,
                        Member.Status.ACTIVE
                )
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        // 5. 토큰 발급 및 리프레시 토큰 Redis 저장
        return issueTokens(member);
    }

    /**
     * 토큰 발급 및 Redis 저장 (공통 로직)
     */
    private AuthRes issueTokens(Member member) {
        String key = String.valueOf(member.getMemberId());
        AuthToken accessToken = authTokenProvider.createUserAppToken(key);
        AuthToken refreshToken = authTokenProvider.createRefreshToken(key);

        redisService.setValuesWithTimeout(key, refreshToken.getToken(), refreshExpiry);

        return AuthRes.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .nickname(member.getNickname())
                .build();
    }
}
