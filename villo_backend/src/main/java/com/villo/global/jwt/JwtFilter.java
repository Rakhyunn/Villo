package com.villo.global.jwt;

import com.villo.global.exception.CustomException;
import com.villo.global.rq.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final Rq rq;

    // 인증 없이 통과할 경로
    private static final List<String> PERMIT_URLS = List.of(
            "/oauth2/",
            "/login/oauth2/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 인증이 필요 없는 경로는 통과
        if (PERMIT_URLS.stream().anyMatch(uri::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Rq로 쿠키에서 토큰 추출
        String accessToken = rq.getCookieValue("accessToken", null);

        // accessToken이 없거나 빈 값이면 인증 없이 통과
        if (accessToken == null || accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 유효성 검증
            if (!jwtProvider.isValid(accessToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰에서 유저 정보 추출
            Long userId = jwtProvider.getUserId(accessToken);

            // SecurityContext에 인증 정보 등록
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (CustomException e) {
            // 만료/위조 토큰은 인증 없이 통과 → SecurityConfig에서 401 처리
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
