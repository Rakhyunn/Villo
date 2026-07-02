package com.villo.domain.village.controller;

import com.villo.domain.village.dto.VillageNameUpdateRequest;
import com.villo.domain.village.dto.VillageResponse;
import com.villo.domain.village.service.VillageService;
import com.villo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/village")
@RequiredArgsConstructor
public class VillageController {
    private final VillageService villageService;

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
}
