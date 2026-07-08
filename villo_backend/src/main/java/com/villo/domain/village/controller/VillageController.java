package com.villo.domain.village.controller;

import com.villo.domain.village.dto.*;
import com.villo.domain.village.entity.type.VillagerGrade;
import com.villo.domain.village.service.VillagePeopleService;
import com.villo.domain.village.service.VillagePlacementService;
import com.villo.domain.village.service.VillageService;
import com.villo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Village", description = "마을 — 조회·이름 변경, 주민 상점·영입, 배치")
@RestController
@RequestMapping("/api/v1/village")
@RequiredArgsConstructor
public class VillageController {
    private final VillageService villageService;
    private final VillagePeopleService villagePeopleService;
    private final VillagePlacementService villagePlacementService;

    // 내 마을 조회
    @Operation(summary = "내 마을 조회", description = "마을 이름·레벨·주민 수 등 조회")
    @GetMapping
    public ApiResponse<VillageResponse> getMyVillage(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("마을 조회 성공", villageService.getMyVillage(userId));
    }

    // 마을 이름 변경
    @Operation(summary = "마을 이름 변경")
    @PutMapping("/name")
    public ApiResponse<VillageResponse> updateVillageName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody VillageNameUpdateRequest request
    ) {
        return ApiResponse.ok("마을 이름이 변경되었습니다.", villageService.updateVillageName(userId, request));
    }

    // 주민 목록 조회
    @Operation(summary = "주민 상점 목록 조회", description = "등급(grade)으로 필터 가능")
    @GetMapping("/people")
    public ApiResponse<List<VillagePeopleResponse>> getVillagePeopleList(
            @RequestParam(required = false) VillagerGrade grade
    ) {
        return ApiResponse.ok("주민 목록 조회 성공", villagePeopleService.getVillagePeopleList(grade));
    }

    // 주민 영입
    @Operation(summary = "주민 영입", description = "골드를 소모해 주민 구매")
    @PostMapping("/people/{villagePeopleId}/buy")
    public ApiResponse<VillagePeopleResponse> buyVillager(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long villagePeopleId
    ) {
        return ApiResponse.ok("주민을 영입했습니다.",
                villagePeopleService.buyVillager(userId, villagePeopleId));
    }

    // 내 보유 주민 목록 (배치 여부 포함)
    @Operation(summary = "내 보유 주민 목록", description = "배치 여부(isPlaced) 포함")
    @GetMapping("/people/my")
    public ApiResponse<List<UserVillagePeopleResponse>> getMyVillagers(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("보유 주민 조회 성공", villagePeopleService.getMyVillagers(userId));
    }

    // 배치 현황 조회
    @Operation(summary = "배치 현황 조회", description = "마을 그리드에 배치된 주민 목록")
    @GetMapping("/placements")
    public ApiResponse<List<VillagePlacementResponse>> getPlacements(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("배치 현황 조회 성공", villagePlacementService.getPlacements(userId));
    }

    // 주민 배치
    @Operation(summary = "주민 배치", description = "지정한 그리드 좌표에 주민 배치")
    @PostMapping("/placements")
    public ApiResponse<VillagePlacementResponse> createPlacement(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PlacementRequest request
    ) {
        return ApiResponse.ok("주민을 배치했습니다.",
                villagePlacementService.createPlacement(userId, request));
    }

    // 배치 위치 변경
    @Operation(summary = "배치 위치 변경", description = "배치된 주민을 다른 좌표로 이동")
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
    @Operation(summary = "배치 해제", description = "배치된 주민을 그리드에서 제거")
    @DeleteMapping("/placements/{placementId}")
    public ApiResponse<Void> deletePlacement(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long placementId
    ) {
        villagePlacementService.deletePlacement(userId, placementId);
        return ApiResponse.ok("배치가 해제되었습니다.");
    }
}
