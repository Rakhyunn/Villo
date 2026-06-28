package com.villo.domain.auth.oauth2;

import com.villo.domain.auth.entity.type.Provider;
import lombok.Getter;

import java.util.Map;

public class OAuth2UserInfo {
    @Getter
    private final Provider provider;
    private final Map<String, Object> attributes;

    public OAuth2UserInfo(Provider provider, Map<String, Object> attributes) {
        this.provider = provider;
        this.attributes = attributes;
    }

    public String getProviderId() {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("sub");
            case KAKAO -> String.valueOf(attributes.get("id"));
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield (String) response.get("id");
            }
        };
    }

    public String getEmail() {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("email");
            case KAKAO -> {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                yield kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            }
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield response != null ? (String) response.get("email") : null;
            }
        };
    }
}
