package com.villo.domain.todo.dto;

public record TodoCompletionResponse(
        int earnedGold,       // 실제 지급된 골드
        int totalGold,        // 누적 전체 골드
        int remainingDaily    // 오늘 남은 골드 한도
) {
}
