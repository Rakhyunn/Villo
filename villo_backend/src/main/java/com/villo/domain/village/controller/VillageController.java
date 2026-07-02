package com.villo.domain.village.controller;

import com.villo.domain.village.dto.VillageNameUpdateRequest;
import com.villo.domain.village.dto.VillagePeopleResponse;
import com.villo.domain.village.dto.VillageResponse;
import com.villo.domain.village.entity.type.VillagerGrade;
import com.villo.domain.village.service.VillagePeopleService;
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
}
