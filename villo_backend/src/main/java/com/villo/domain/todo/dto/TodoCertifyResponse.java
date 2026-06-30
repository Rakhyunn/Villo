package com.villo.domain.todo.dto;

public record TodoCertifyResponse(
        int earnedGold,
        int totalGold,
        int remainingDaily
) {
}
