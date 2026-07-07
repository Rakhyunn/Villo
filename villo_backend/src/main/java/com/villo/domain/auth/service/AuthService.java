package com.villo.domain.auth.service;

import com.villo.domain.auth.dto.NicknameRequest;
import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.repository.UserVillageRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import com.villo.global.jwt.JwtProvider;
import com.villo.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserVillageRepository userVillageRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;

    // 닉네임 중복 확인
    public void checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    // 닉네임 + 마을 이름 최초 설정
    @Transactional
    public void setNickname(Long userId, NicknameRequest request) {
        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 설정
        user.updateNickname(request.nickname());

        // 마을 이름 설정 (입력값 있으면 변경, 없으면 "나의 마을" 유지)
        if (request.villageName() != null && !request.villageName().isBlank()) {
            UserVillage village = userVillageRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.VILLAGE_NOT_FOUND));
            village.updateVillageName(request.villageName());
        }
    }

    // Access Token 재발급 (Refresh Token Rotation)
    public void reissueToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtProvider.isValid(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        // Redis에 저장된 토큰과 비교
        if (!refreshTokenService.isValid(userId, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 새 토큰 발급
        String newAccessToken = jwtProvider.generateAccessToken(userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        // Refresh Token Rotation
        refreshTokenService.rotate(userId, newRefreshToken);

        // 쿠키 설정
        rq.setAccessTokenCookie(newAccessToken);
        rq.setRefreshTokenCookie(newRefreshToken);
    }

    // 로그아웃
    public void logout(Long userId) {
        refreshTokenService.delete(userId);
        rq.deleteAuthCookies();
    }
}
