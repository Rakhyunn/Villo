package com.villo.domain.village.dto;

import com.villo.domain.village.entity.VillagePeople;
import com.villo.domain.village.entity.type.VillagerGrade;

public record VillagePeopleResponse(
        Long id,
        String name,
        VillagerGrade grade,
        int price,
        String imageUrl,
        String description
) {
    public static VillagePeopleResponse from(VillagePeople villagePeople) {
        return new VillagePeopleResponse(
                villagePeople.getId(),
                villagePeople.getName(),
                villagePeople.getGrade(),
                villagePeople.getPrice(),
                villagePeople.getImageUrl(),
                villagePeople.getDescription()
        );
    }
}
