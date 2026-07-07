package com.villo.domain.todo.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DailyGoldLimit {
    LEVEL_1(1, 500),
    LEVEL_2(2, 700),
    LEVEL_3(3, 1000);

    private final int level;
    private final int limit;

    public static int getLimitByLevel(int villageLevel) {
        for (DailyGoldLimit goldLimit : values()) {
            if (goldLimit.level == villageLevel) {
                return goldLimit.limit;
            }
        }
        return LEVEL_1.limit;
    }
}
