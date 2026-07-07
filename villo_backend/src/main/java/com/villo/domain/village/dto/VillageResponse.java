package com.villo.domain.village.dto;

import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.entity.type.VillageLevelPolicy;

public record VillageResponse(
        Long id,
        String villageName,
        int villageLevel,
        int gridSize,
        int villagerCount,
        Integer nextLevelThreshold
) {
    public static VillageResponse of(UserVillage village, int villagerCount) {
        int level = village.getVillageLevel();
        return new VillageResponse(
                village.getId(),
                village.getVillageName(),
                level,
                VillageLevelPolicy.getGridSize(level),
                villagerCount,
                VillageLevelPolicy.getNextLevelThreshold(level)
        );
    }
}
