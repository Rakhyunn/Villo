package com.villo.domain.todo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TodoCertifyRequest(
        @NotEmpty(message = "인증 사진을 1장 이상 첨부해주세요.")
        @Size(max = 3, message = "사진은 최대 3장까지 첨부 가능합니다.")
        List<String> imageUrls
) {
}
