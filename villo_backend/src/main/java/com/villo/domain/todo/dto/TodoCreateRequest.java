package com.villo.domain.todo.dto;

import com.villo.domain.todo.entity.type.Difficulty;
import jakarta.validation.constraints.*;

public record TodoCreateRequest(
        @NotBlank(message = "퀘스트를 입력해주세요.")
        @Size(max = 200, message = "퀘스트 이름은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "카테고리를 입력해주세요.")
        String category,

        @NotNull(message = "난이도를 입력해주세요.")
        Difficulty difficulty,

        @Min(value = 1, message = "골드는 1 이상이어야 합니다.")
        @Max(value = 200, message = "골드는 200 이하여야 합니다.")
        int gold,

        boolean isRepeat,

        RepeatConfigRequest repeatConfig
) {
}
