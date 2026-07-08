package com.villo.domain.mypage.controller;

import com.villo.domain.mypage.dto.*;
import com.villo.domain.mypage.service.MyPageService;
import com.villo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "MyPage", description = "마이페이지 — 프로필·통계, 닉네임 변경, 완료 달력")
@RestController
@RequestMapping("/api/v1/my")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    // 프로필 조회
    @Operation(summary = "프로필 조회", description = "닉네임·이메일·소셜·골드 조회")
    @GetMapping("/profile")
    public ApiResponse<MyPageResponse> getProfile(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("프로필 조회 성공", myPageService.getProfile(userId));
    }

    // 통계 조회
    @Operation(summary = "통계 조회", description = "완료 퀘스트 수·연속 달성일·보유 주민 수")
    @GetMapping("/stats")
    public ApiResponse<MyStatsResponse> getStats(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("통계 조회 성공", myPageService.getStats(userId));
    }

    // 닉네임 변경
    @Operation(summary = "닉네임 변경")
    @PutMapping("/nickname")
    public ApiResponse<Void> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        myPageService.updateNickname(userId, request.nickname());
        return ApiResponse.ok("닉네임이 변경되었습니다.");
    }

    // 월별 완료 날짜 목록 (달력 점 표시용)
    @Operation(summary = "월별 완료 날짜 조회", description = "달력 점 표시용 완료 날짜 목록")
    @GetMapping("/calendar")
    public ApiResponse<MyCalendarResponse> getCalendar(
            @AuthenticationPrincipal Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ApiResponse.ok("달력 조회 성공", myPageService.getCalendar(userId, year, month));
    }

    // 날짜별 완료 투두 목록
    @Operation(summary = "날짜별 완료 투두 조회", description = "특정 날짜의 완료 퀘스트·인증 사진")
    @GetMapping("/todos")
    public ApiResponse<List<MyDailyTodoResponse>> getDailyTodos(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok("일별 완료 퀘스트 조회 성공", myPageService.getDailyTodos(userId, date));
    }
}
