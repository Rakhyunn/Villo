package com.villo.domain.todo.dto;

import com.villo.domain.todo.entity.RepeatConfig;
import com.villo.domain.todo.entity.type.RepeatType;

import java.time.LocalDate;

public record RepeatConfigResponse(
        Long id,
        RepeatType repeatType,
        String repeatValue,
        LocalDate endDate
) {
    public static RepeatConfigResponse from(RepeatConfig config) {
        return new RepeatConfigResponse(
                config.getId(),
                config.getRepeatType(),
                config.getRepeatValue(),
                config.getEndDate()
        );
    }
}
