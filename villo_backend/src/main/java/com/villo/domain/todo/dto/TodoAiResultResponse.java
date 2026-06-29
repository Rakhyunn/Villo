package com.villo.domain.todo.dto;

import com.villo.domain.todo.entity.type.Difficulty;

public record TodoAiResultResponse(
        String category,
        Difficulty difficulty,
        int gold
) {
}
