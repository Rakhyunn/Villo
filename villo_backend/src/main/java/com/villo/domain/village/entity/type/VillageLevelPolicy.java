package com.villo.domain.village.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VillageLevelPolicy {
    LEVEL_1(1, 5, 5),
    LEVEL_2(2, 7, 8),
    LEVEL_3(3, 10, null);

    private final int level;
    private final int gridSize;
    private final Integer nextLevelThreshold; // null이면 최대 레벨

    public static int getGridSize(int level) {
        return findByLevel(level).gridSize;
    }

    public static Integer getNextLevelThreshold(int level) {
        return findByLevel(level).nextLevelThreshold;
    }

    private static VillageLevelPolicy findByLevel(int level) {
        for (VillageLevelPolicy policy : values()) {
            if (policy.level == level) {
                return policy;
            }
        }
        return LEVEL_1;
    }
}
