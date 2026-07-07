package com.villo.domain.village.service;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.village.dto.UserVillagePeopleResponse;
import com.villo.domain.village.dto.VillagePeopleResponse;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.entity.UserVillagePeople;
import com.villo.domain.village.entity.VillagePeople;
import com.villo.domain.village.entity.type.VillageLevelPolicy;
import com.villo.domain.village.entity.type.VillagerGrade;
import com.villo.domain.village.repository.UserVillagePeopleRepository;
import com.villo.domain.village.repository.UserVillageRepository;
import com.villo.domain.village.repository.VillagePeopleRepository;
import com.villo.domain.village.repository.VillagePlacementRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VillagePeopleService {
    private final VillagePeopleRepository villagePeopleRepository;
    private final UserVillageRepository userVillageRepository;
    private final UserVillagePeopleRepository userVillagePeopleRepository;
    private final UserRepository userRepository;
    private final VillagePlacementRepository villagePlacementRepository;

    // 내 보유 주민 목록 조회 (배치 여부 포함)
    @Transactional(readOnly = true)
    public List<UserVillagePeopleResponse> getMyVillagers(Long userId) {
        List<UserVillagePeople> myVillagers = userVillagePeopleRepository.findByUserId(userId);

        UserVillage userVillage = userVillageRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VILLAGE_NOT_FOUND));

        // 배치된 UserVillagePeople ID 목록 조회
        Set<Long> placedIds = villagePlacementRepository
                .findByUserVillageId(userVillage.getId())
                .stream()
                .map(placement -> placement.getUserVillagePeople().getId())
                .collect(Collectors.toSet());

        return myVillagers.stream()
                .map(uvp -> UserVillagePeopleResponse.of(uvp, placedIds.contains(uvp.getId())))
                .toList();
    }

    // 주민 목록 조회 (등급별 필터, null이면 전체)
    @Transactional(readOnly = true)
    public List<VillagePeopleResponse> getVillagePeopleList(VillagerGrade grade) {
        List<VillagePeople> villagePeopleList = (grade == null)
                ? villagePeopleRepository.findByIsActiveTrue()
                : villagePeopleRepository.findByGradeAndIsActiveTrue(grade);

        return villagePeopleList.stream()
                .map(VillagePeopleResponse::from)
                .toList();
    }

    // 주민 영입
    @Transactional
    public VillagePeopleResponse buyVillager(Long userId, Long villagePeopleId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        VillagePeople villagePeople = villagePeopleRepository.findById(villagePeopleId)
                .orElseThrow(() -> new CustomException(ErrorCode.VILLAGER_NOT_FOUND));

        if (!villagePeople.isActive()) {
            throw new CustomException(ErrorCode.VILLAGER_NOT_FOUND);
        }

        // 골드 부족 체크
        if (user.getTotalGold() < villagePeople.getPrice()) {
            throw new CustomException(ErrorCode.NOT_ENOUGH_GOLD);
        }

        // 골드 차감
        user.decreaseGold(villagePeople.getPrice());

        // 보유 주민 추가
        UserVillagePeople userVillagePeople = UserVillagePeople.builder()
                .user(user)
                .villagePeople(villagePeople)
                .build();
        userVillagePeopleRepository.save(userVillagePeople);

        // 마을 레벨 자동 확장 체크
        checkAndUpgradeVillageLevel(userId);

        return VillagePeopleResponse.from(villagePeople);
    }

    // 보유 주민 수에 따른 마을 레벨 자동 확장
    private void checkAndUpgradeVillageLevel(Long userId) {
        UserVillage village = userVillageRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VILLAGE_NOT_FOUND));

        int villagerCount = userVillagePeopleRepository.countByUserId(userId);
        int currentLevel = village.getVillageLevel();

        Integer requiredForNextLevel = VillageLevelPolicy.getNextLevelThreshold(currentLevel);

        if (requiredForNextLevel != null && villagerCount >= requiredForNextLevel) {
            village.levelUp();
        }
    }
}
