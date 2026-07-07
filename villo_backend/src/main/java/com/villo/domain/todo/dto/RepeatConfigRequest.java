package com.villo.domain.todo.dto;

import com.villo.domain.todo.entity.type.RepeatType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RepeatConfigRequest(
        @NotNull(message = "반복 주기를 선택해주세요.")
        RepeatType repeatType,

        String repeatValue, // WEEKLY: "MON,WED,FRI" / MONTHLY: "1,15" / DAILY: null

        LocalDate endDate   // 선택 (null이면 무기한)
) {
}
