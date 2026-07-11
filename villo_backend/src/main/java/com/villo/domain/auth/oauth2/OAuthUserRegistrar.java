package com.villo.domain.auth.oauth2;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.repository.UserVillageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 소셜 로그인 사용자 find-or-create 공용 로직.
 * 웹(OAuth2 리다이렉트)과 모바일(토큰 교환)이 동일한 규칙으로
 * 유저 조회/신규 가입 + 마을 자동 생성을 처리하도록 한 곳에 모은다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthUserRegistrar {
    private final UserRepository userRepository;
    private final UserVillageRepository userVillageRepository;

    // provider + providerId로 기존 유저 조회, 없으면 신규 가입
    @Transactional
    public User findOrCreate(OAuth2UserInfo userInfo) {
        return userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .orElseGet(() -> create(userInfo));
    }

    // 신규 유저 등록 + 마을 자동 생성
    private User create(OAuth2UserInfo userInfo) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .nickname(null)
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .totalGold(0)
                .dailyGold(0)
                .lastGoldResetDate(LocalDate.now())
                .build();

        User savedUser = userRepository.save(user);

        // 마을 자동 생성
        UserVillage village = UserVillage.builder()
                .user(savedUser)
                .villageName("나의 마을")
                .villageLevel(1)
                .build();

        userVillageRepository.save(village);

        log.info("신규 유저 가입: provider={}, providerId={}", userInfo.getProvider(), userInfo.getProviderId());

        return savedUser;
    }
}
