package com.villo.domain.village.dto;

import com.villo.domain.village.entity.VillagePlacement;

public record VillagePlacementResponse(
        Long id,
        Long userVillagePeopleId,
        String villagerName,
        String villagerImageUrl,
        int gridX,
        int gridY
) {
    public static VillagePlacementResponse from(VillagePlacement placement) {
        return new VillagePlacementResponse(
                placement.getId(),
                placement.getUserVillagePeople().getId(),
                placement.getUserVillagePeople().getVillagePeople().getName(),
                placement.getUserVillagePeople().getVillagePeople().getImageUrl(),
                placement.getGridX(),
                placement.getGridY()
        );
    }
}
