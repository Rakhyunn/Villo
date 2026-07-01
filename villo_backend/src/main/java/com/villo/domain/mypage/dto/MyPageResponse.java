package com.villo.domain.mypage.dto;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.entity.type.Provider;

public record MyPageResponse(
        Long id,
        String nickname,
        String email,
        Provider provider,
        int totalGold,
        int dailyGold
) {
    public static MyPageResponse from(User user) {
        return new MyPageResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProvider(),
                user.getTotalGold(),
                user.getDailyGold()
        );
    }
}
