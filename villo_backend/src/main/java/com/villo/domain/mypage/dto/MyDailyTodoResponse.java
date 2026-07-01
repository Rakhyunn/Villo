package com.villo.domain.mypage.dto;

import com.villo.domain.todo.entity.TodoCompletion;

import java.util.List;

public record MyDailyTodoResponse(
        Long todoId,
        String title,
        String category,
        int earnedGold,
        boolean isCertified,
        List<String> imageUrls
) {
    public static MyDailyTodoResponse from(TodoCompletion completion) {
        return new MyDailyTodoResponse(
                completion.getTodo().getId(),
                completion.getTodo().getTitle(),
                completion.getTodo().getCategory(),
                completion.getEarnedGold(),
                completion.isCertified(),
                completion.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .toList()
        );
    }
}
