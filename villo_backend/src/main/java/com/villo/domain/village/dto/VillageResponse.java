package com.villo.domain.village.dto;

import com.villo.domain.village.entity.UserVillage;

public record VillageResponse(
        Long id,
        String villageName,
        int villageLevel,
        int gridSize    // 레벨에 따른 그리드 크기(5, 7, 10)
) {
    public static VillageResponse from(UserVillage village) {
        return new VillageResponse(
                village.getId(),
                village.getVillageName(),
                village.getVillageLevel(),
                calculateGridSize(village.getVillageLevel())
        );
    }

    private static int calculateGridSize(int level) {
        return switch (level) {
            case 2 -> 7;
            case 3 -> 10;
            default -> 5;
        };
    }
}
