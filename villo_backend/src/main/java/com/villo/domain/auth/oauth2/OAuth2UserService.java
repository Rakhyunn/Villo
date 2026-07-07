package com.villo.domain.auth.oauth2;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.entity.type.Provider;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.repository.UserVillageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final UserVillageRepository userVillageRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 유저 정보 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 소셜 플랫폼 정보 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        // 필요한 유저 정보 추출
        OAuth2UserInfo userInfo = new OAuth2UserInfo(provider, oAuth2User.getAttributes());

        // DB에서 유저 조회 또는 신규 가입
        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> createOAuthUser(userInfo));

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    // 신규 유저 등록
    private User createOAuthUser(OAuth2UserInfo userInfo) {
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
