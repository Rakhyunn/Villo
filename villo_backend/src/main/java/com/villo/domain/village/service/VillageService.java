package com.villo.domain.village.service;

import com.villo.domain.village.dto.VillageNameUpdateRequest;
import com.villo.domain.village.dto.VillageResponse;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.repository.UserVillagePeopleRepository;
import com.villo.domain.village.repository.UserVillageRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VillageService {
    private final UserVillageRepository userVillageRepository;
    private final UserVillagePeopleRepository userVillagePeopleRepository;

    // 내 마을 조회
    @Transactional(readOnly = true)
    public VillageResponse getMyVillage(Long userId) {
        UserVillage village = getVillageByUserId(userId);
        int villagerCount = userVillagePeopleRepository.countByUserId(userId);
        return VillageResponse.of(village, villagerCount);
    }

    // 마을 이름 변경
    @Transactional
    public VillageResponse updateVillageName(Long userId, VillageNameUpdateRequest request) {
        UserVillage village = getVillageByUserId(userId);
        village.updateVillageName(request.villageName());
        int villagerCount = userVillagePeopleRepository.countByUserId(userId);
        return VillageResponse.of(village, villagerCount);
    }

    // (공통) 유저 마을 조회
    public UserVillage getVillageByUserId(Long userId) {
        return userVillageRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VILLAGE_NOT_FOUND));
    }
}
