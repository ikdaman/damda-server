package com.damda.domain.auth.service;

import com.damda.global.auth.client.SocialTokenClient;
import com.damda.global.auth.enumerate.Provider;
import com.damda.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.damda.global.exception.ErrorCode.INVALID_SOCIAL_PROVIDER;

/**
 * Provider에 맞는 SocialTokenClient를 찾아 토큰을 검증하고 providerId를 추출
 */
@Service
@RequiredArgsConstructor
public class SocialTokenValidator {

    private final List<SocialTokenClient> clients;

    public String validate(Provider provider, String token) {
        SocialTokenClient client = clients.stream()
                .filter(c -> c.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new BaseException(INVALID_SOCIAL_PROVIDER));
        return client.extractProviderId(token);
    }
}
