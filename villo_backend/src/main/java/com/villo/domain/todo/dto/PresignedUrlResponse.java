package com.villo.domain.todo.dto;

public record PresignedUrlResponse(
        String uploadUrl,  // 프론트가 PUT 요청 보낼 URL
        String imageUrl    // 업로드 완료 후 실제 이미지 접근 URL
) {
}
