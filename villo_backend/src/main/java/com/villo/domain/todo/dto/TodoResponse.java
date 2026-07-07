package com.villo.domain.todo.dto;

import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.entity.type.Difficulty;
import com.villo.domain.todo.entity.type.TodoStatus;

import java.time.LocalDateTime;

public record TodoResponse(
        Long id,
        String title,
        String category,
        Difficulty difficulty,
        int gold,
        boolean isRepeat,
        TodoStatus status,
        LocalDateTime createdDate
) {
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getCategory(),
                todo.getDifficulty(),
                todo.getGold(),
                todo.isRepeat(),
                todo.getStatus(),
                todo.getCreatedDate()
        );
    }
}
