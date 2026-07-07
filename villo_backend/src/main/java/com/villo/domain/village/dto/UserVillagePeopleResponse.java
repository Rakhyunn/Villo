package com.villo.domain.village.dto;

import com.villo.domain.village.entity.UserVillagePeople;
import com.villo.domain.village.entity.type.VillagerGrade;

public record UserVillagePeopleResponse(
        Long userVillagePeopleId,
        String name,
        VillagerGrade grade,
        String imageUrl,
        boolean isPlaced
) {
    public static UserVillagePeopleResponse of(UserVillagePeople uvp, boolean isPlaced) {
        return new UserVillagePeopleResponse(
                uvp.getId(),
                uvp.getVillagePeople().getName(),
                uvp.getVillagePeople().getGrade(),
                uvp.getVillagePeople().getImageUrl(),
                isPlaced
        );
    }
}
