package com.villo.domain.village.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PlacementRequest(
        @NotNull(message = "배치할 주민을 선택해주세요.")
        Long userVillagePeopleId,

        @Min(value = 0, message = "좌표는 0 이상이어야 합니다.")
        int gridX,

        @Min(value = 0, message = "좌표는 0 이상이어야 합니다.")
        int gridY
) {
}
