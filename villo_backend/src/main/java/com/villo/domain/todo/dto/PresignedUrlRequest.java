package com.villo.domain.todo.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
        @NotBlank(message = "파일 이름을 입력해주세요.")
        String fileName
) {
}
