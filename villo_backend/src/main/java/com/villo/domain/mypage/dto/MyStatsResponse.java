package com.villo.domain.mypage.dto;

public record MyStatsResponse(
        int completedTodoCount, // 완료 퀘스트 수
        int consecutiveDays,    // 연속 달성일
        int villagerCount       // 보유 주민 수
) {
}
