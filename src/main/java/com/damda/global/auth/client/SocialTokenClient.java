package com.damda.global.auth.client;

import com.damda.global.auth.enumerate.Provider;

/**
 * 소셜 토큰 검증/파싱
 */
public interface SocialTokenClient {
    Provider provider();
    String extractProviderId(String token);
}
