package com.damda.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Refresh Token 저장
     */
    public void setValuesWithTimeout(String key, String value, long timeout) {  // key:value = memberId:RefreshToken
        String redisKey = "FMRT: " + key;
        redisTemplate.opsForValue().set(redisKey, value, timeout, TimeUnit.MILLISECONDS);
    }

    @Transactional(readOnly = true)
    public Optional<String> getValues(String key) {
        String redisKey = "FMRT: " + key;
        return Optional.ofNullable(redisTemplate.opsForValue().get(redisKey));
    }

    public void deleteValues(String key) {
        String redisKey = "FMRT: " + key;
        redisTemplate.delete(redisKey);
    }
}
