package com.villo.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refresh:";
    private static final Duration TTL = Duration.ofDays(7);
    private static final Duration GRACE_PERIOD = Duration.ofSeconds(60);

    // Refresh Token 저장
    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                getKey(userId),
                refreshToken,
                TTL
        );
    }

    // Refresh Token 조회
    public String get(Long userId) {
        return redisTemplate.opsForValue().get(getKey(userId));
    }

    // Refresh Token 삭제 (로그아웃)
    public void delete(Long userId) {
        redisTemplate.delete(getKey(userId));
    }

    // Refresh Token Rotation — 기존 토큰 유예기간 후 새 토큰 저장
    public void rotate(Long userId, String newRefreshToken) {
        // 기존 토큰 유예기간 설정 (동시 요청 대응)
        String oldToken = get(userId);
        if (oldToken != null) {
            redisTemplate.opsForValue().set(
                    getGraceKey(userId),
                    oldToken,
                    GRACE_PERIOD
            );
        }
        // 새 토큰 저장
        save(userId, newRefreshToken);
    }

    // Redis에 저장된 토큰과 일치하는지 확인
    public boolean isValid(Long userId, String refreshToken) {
        String stored = get(userId);
        if (stored != null && stored.equals(refreshToken)) return true;

        // 유예기간 토큰도 확인
        String grace = redisTemplate.opsForValue().get(getGraceKey(userId));
        return grace != null && grace.equals(refreshToken);
    }

    private String getKey(Long userId) {
        return PREFIX + userId;
    }

    private String getGraceKey(Long userId) {
        return PREFIX + "grace:" + userId;
    }
}
