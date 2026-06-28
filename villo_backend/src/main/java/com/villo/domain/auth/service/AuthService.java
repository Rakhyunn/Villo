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

        // 새 토큰 재발급
        String accessToken = jwtProvider.generateAccessToken(userId);
        String refreshToken = jwtProvider.generateRefreshToken(userId);
        rq.setAccessTokenCookie(accessToken);
        rq.setRefreshTokenCookie(refreshToken);
    }
}
