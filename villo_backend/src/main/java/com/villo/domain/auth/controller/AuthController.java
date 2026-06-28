package com.villo.domain.auth.controller;

import com.villo.domain.auth.dto.NicknameRequest;
import com.villo.domain.auth.service.AuthService;
import com.villo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 닉네임 중복 확인
    @GetMapping("/nickname/check")
    public ApiResponse<Void> checkNickname(
            @RequestParam String nickname
    ) {
        authService.checkNickname(nickname);
        return ApiResponse.ok("사용 가능한 닉네임입니다.");
    }

    // 닉네임 + 마을 이름 최초 설정
    @PostMapping("/nickname")
    public ApiResponse<Void> setNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameRequest request
    ) {
        authService.setNickname(userId, request);
        return ApiResponse.ok("닉네임 설정이 완료되었습니다.");
    }
}
