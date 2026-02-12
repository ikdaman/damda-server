package com.damda.global.auth.enumerate;

import com.damda.global.exception.BaseException;

import static com.damda.global.exception.ErrorCode.INVALID_SOCIAL_PROVIDER;

/**
 * 소셜 로그인 제공자
 */
public enum Provider {
    KAKAO, NAVER, GOOGLE, APPLE;

    /**
     * 문자열 to Enum
     * @param name  제공자 문자열
     * @return  매칭되는 Provider 값
     */
    public static Provider from(String name) {
        if (name == null) throw new BaseException(INVALID_SOCIAL_PROVIDER);
        return Provider.valueOf(name.trim().toUpperCase());
    }
}
