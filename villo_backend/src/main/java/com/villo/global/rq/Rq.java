package com.villo.global.rq;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequestScope
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.domain:localhost}")
    private String cookieDomain;

    @Value("${cookie.same-site:Lax}")
    private String cookieSameSite;

    // 쿠키에서 값 추출
    public String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(req.getCookies())
                                        .filter(cookie -> name.equals(cookie.getName()))
                                        .map(Cookie::getValue)
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    // 쿠키 설정
    public void setCookie(String name, String value, int maxAge) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 쿠키를 도메인 전체에서 쓰겠다.
        cookie.setHttpOnly(true); // 쿠키를 스크립트로 접근 못하게(XSS 공격방어)
        // 쿠키가 적용될 도메인 지정
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setSecure(cookieSecure); // https 에서만 쿠키전송
        cookie.setAttribute("SameSite", cookieSameSite); // 동일 사이트에서만 쿠키 전송(CSRF 공격방어)
        // 값이 없다면 해당 변수를 삭제하라는 뜻
        cookie.setMaxAge(value.isBlank() ? 0 : maxAge);

        resp.addCookie(cookie);
    }

    // Access Token 쿠키 설정 (1시간)
    public void setAccessTokenCookie(String token) {
        setCookie("accessToken", token, 60 * 60);
    }

    // Refresh Token 쿠키 설정 (7일)
    public void setRefreshTokenCookie(String token) {
        setCookie("refreshToken", token, 60 * 60 * 24 * 7);
    }

    // 쿠키 삭제
    public void deleteCookie(String name) {
        setCookie(name, "", 0);
    }

    // Access Token / Refresh Token 쿠키 모두 삭제 (로그아웃)
    public void deleteAuthCookies() {
        deleteCookie("accessToken");
        deleteCookie("refreshToken");
    }
}
