package com.villo.domain.village.service;

import com.villo.domain.village.dto.PlacementRequest;
import com.villo.domain.village.dto.VillagePlacementResponse;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.entity.UserVillagePeople;
import com.villo.domain.village.entity.VillagePlacement;
import com.villo.domain.village.repository.UserVillagePeopleRepository;
import com.villo.domain.village.repository.UserVillageRepository;
import com.villo.domain.village.repository.VillagePlacementRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VillagePlacementService {
    private final VillagePlacementRepository villagePlacementRepository;
    private final UserVillageRepository userVillageRepository;
    private final UserVillagePeopleRepository userVillagePeopleRepository;

    // 배치 현황 조회
    @Transactional(readOnly = true)
    public List<VillagePlacementResponse> getPlacements(Long userId) {
        UserVillage village = getVillageByUserId(userId);

        return villagePlacementRepository.findByUserVillageId(village.getId())
                .stream()
                .map(VillagePlacementResponse::from)
                .toList();
    }

    // 주민 배치
    @Transactional
    public VillagePlacementResponse createPlacement(Long userId, PlacementRequest request) {
        UserVillage village = getVillageByUserId(userId);

        // 보유 주민 확인 (본인 소유인지)
        UserVillagePeople userVillagePeople = userVillagePeopleRepository
                .findById(request.userVillagePeopleId())
                .orElseThrow(() -> new CustomException(ErrorCode.VILLAGER_NOT_FOUND));

        if (!userVillagePeople.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 그리드 범위 검증
        int gridSize = calculateGridSize(village.getVillageLevel());
        validateGridRange(request.gridX(), request.gridY(), gridSize);

        // 이미 배치된 주민인지 체크
        if (villagePlacementRepository.existsByUserVillagePeopleId(request.userVillagePeopleId())) {
            throw new CustomException(ErrorCode.ALREADY_PLACED_VILLAGER);
        }

        // 이미 배치된 좌표인지 체크
        if (villagePlacementRepository.existsByUserVillageIdAndGridXAndGridY(
                village.getId(), request.gridX(), request.gridY())) {
            throw new CustomException(ErrorCode.ALREADY_OCCUPIED_TILE);
        }

        VillagePlacement placement = VillagePlacement.builder()
                .userVillage(village)
                .userVillagePeople(userVillagePeople)
                .gridX(request.gridX())
                .gridY(request.gridY())
                .build();

        return VillagePlacementResponse.from(villagePlacementRepository.save(placement));
    }

    // 배치 위치 변경
    @Transactional
    public VillagePlacementResponse updatePlacement(Long userId, Long placementId, PlacementRequest request) {
        UserVillage village = getVillageByUserId(userId);

        VillagePlacement placement = villagePlacementRepository.findById(placementId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

        if (!placement.getUserVillage().getId().equals(village.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        int gridSize = calculateGridSize(village.getVillageLevel());
        validateGridRange(request.gridX(), request.gridY(), gridSize);

        // 이동하려는 좌표가 이미 다른 주민이 있는 칸인지 체크 (본인 제외)
        villagePlacementRepository
                .findByUserVillageIdAndGridXAndGridY(village.getId(), request.gridX(), request.gridY())
                .filter(existing -> !existing.getId().equals(placementId))
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.ALREADY_OCCUPIED_TILE);
                });

        placement.updatePosition(request.gridX(), request.gridY());

        return VillagePlacementResponse.from(placement);
    }

    // 배치 해제
    @Transactional
    public void deletePlacement(Long userId, Long placementId) {
        UserVillage village = getVillageByUserId(userId);

        VillagePlacement placement = villagePlacementRepository.findById(placementId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACEMENT_NOT_FOUND));

        if (!placement.getUserVillage().getId().equals(village.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        villagePlacementRepository.delete(placement);
    }

    // 그리드 크기 계산
    private int calculateGridSize(int level) {
        return switch (level) {
            case 1 -> 5;
            case 2 -> 7;
            case 3 -> 10;
            default -> 5;
        };
    }

    // 좌표 범위 검증
    private void validateGridRange(int gridX, int gridY, int gridSize) {
        if (gridX < 0 || gridX >= gridSize || gridY < 0 || gridY >= gridSize) {
            throw new CustomException(ErrorCode.INVALID_GRID_POSITION);
        }
    }

    private UserVillage getVillageByUserId(Long userId) {
        return userVillageRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VILLAGE_NOT_FOUND));
    }
}
