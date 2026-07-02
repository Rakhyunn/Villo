package com.villo.domain.village.controller;

import com.villo.domain.village.dto.*;
import com.villo.domain.village.entity.type.VillagerGrade;
import com.villo.domain.village.service.VillagePeopleService;
import com.villo.domain.village.service.VillagePlacementService;
import com.villo.domain.village.service.VillageService;
import com.villo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/village")
@RequiredArgsConstructor
public class VillageController {
    private final VillageService villageService;
    private final VillagePeopleService villagePeopleService;
    private final VillagePlacementService villagePlacementService;

    // 내 마을 조회
    @GetMapping
    public ApiResponse<VillageResponse> getMyVillage(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("마을 조회 성공", villageService.getMyVillage(userId));
    }

    // 마을 이름 변경
    @PutMapping("/name")
    public ApiResponse<VillageResponse> updateVillageName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody VillageNameUpdateRequest request
    ) {
        return ApiResponse.ok("마을 이름이 변경되었습니다.", villageService.updateVillageName(userId, request));
    }

    // 주민 목록 조회
    @GetMapping("/people")
    public ApiResponse<List<VillagePeopleResponse>> getVillagePeopleList(
            @RequestParam(required = false) VillagerGrade grade
    ) {
        return ApiResponse.ok("주민 목록 조회 성공", villagePeopleService.getVillagePeopleList(grade));
    }

    // 주민 영입
    @PostMapping("/people/{villagePeopleId}/buy")
    public ApiResponse<VillagePeopleResponse> buyVillager(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long villagePeopleId
    ) {
        return ApiResponse.ok("주민을 영입했습니다.",
                villagePeopleService.buyVillager(userId, villagePeopleId));
    }

    // 배치 현황 조회
    @GetMapping("/placements")
    public ApiResponse<List<VillagePlacementResponse>> getPlacements(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("배치 현황 조회 성공", villagePlacementService.getPlacements(userId));
    }

    // 주민 배치
    @PostMapping("/placements")
    public ApiResponse<VillagePlacementResponse> createPlacement(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PlacementRequest request
    ) {
        return ApiResponse.ok("주민을 배치했습니다.",
                villagePlacementService.createPlacement(userId, request));
    }

    // 배치 위치 변경
    @PutMapping("/placements/{placementId}")
    public ApiResponse<VillagePlacementResponse> updatePlacement(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long placementId,
            @Valid @RequestBody PlacementRequest request
    ) {
        return ApiResponse.ok("배치 위치가 변경되었습니다.",
                villagePlacementService.updatePlacement(userId, placementId, request));
    }

    // 배치 해제
    @DeleteMapping("/placements/{placementId}")
    public ApiResponse<Void> deletePlacement(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long placementId
    ) {
        villagePlacementService.deletePlacement(userId, placementId);
        return ApiResponse.ok("배치가 해제되었습니다.");
    }
}
