package com.villo.domain.auth.oauth2;

import com.villo.domain.auth.entity.type.Provider;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * 모바일 소셜 로그인용 provider access token 검증기.
 * 앱이 네이티브 SDK로 받은 provider 토큰을 넘기면,
 * 서버가 각 provider의 userinfo 엔드포인트를 직접 호출해 토큰을 검증하고
 * 사용자 정보({@link OAuth2UserInfo})를 추출한다. (웹 리다이렉트 플로우와 무관)
 */
@Component
@Slf4j
public class OAuth2TokenVerifier {
    private final RestClient restClient = RestClient.create();

    // provider별 사용자 정보 조회 엔드포인트 — Bearer access token으로 호출
    private static final Map<Provider, String> USER_INFO_URIS = Map.of(
            Provider.KAKAO, "https://kapi.kakao.com/v2/user/me",
            Provider.NAVER, "https://openapi.naver.com/v1/nid/me",
            Provider.GOOGLE, "https://www.googleapis.com/oauth2/v3/userinfo"
    );

    // provider access token을 검증하고 사용자 정보를 추출한다.
    @SuppressWarnings("unchecked")
    public OAuth2UserInfo verify(Provider provider, String accessToken) {
        String uri = USER_INFO_URIS.get(provider);

        try {
            Map<String, Object> attributes = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            if (attributes == null || attributes.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            OAuth2UserInfo userInfo = new OAuth2UserInfo(provider, attributes);

            // providerId 추출 실패(응답에 식별자 없음) → 유효하지 않은 토큰으로 간주
            String providerId = userInfo.getProviderId();
            if (providerId == null || providerId.isBlank()) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            return userInfo;
        } catch (RestClientException e) {
            // 만료·위조 토큰 등으로 provider가 4xx/5xx 응답 → 인증 실패
            log.warn("소셜 토큰 검증 실패: provider={}, error={}", provider, e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
