package com.villo.domain.todo.dto;

import com.villo.domain.todo.entity.type.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoUpdateRequest(
        @NotBlank(message = "퀘스트를 입력해주세요.")
        @Size(max = 200, message = "퀘스트 이름은 200자 이하여야 합니다.")
        String title,
        String category,
        Difficulty difficulty,
        Integer gold
) {
}
