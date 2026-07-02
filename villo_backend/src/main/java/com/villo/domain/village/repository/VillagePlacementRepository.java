package com.villo.domain.village.repository;

import com.villo.domain.village.entity.VillagePlacement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VillagePlacementRepository extends JpaRepository<VillagePlacement, Long> {
    // 마을 배치 현황 조회
    List<VillagePlacement> findByUserVillageId(Long userVillageId);

    // 유저의 주민인지 확인
    boolean existsByUserVillagePeopleId(Long userVillagePeopleId);

    // 유효한 범위의 좌표인지 확인
    boolean existsByUserVillageIdAndGridXAndGridY(Long userVillageId, int gridX, int gridY);

    // 주민이 있는 좌표인지 확인
    Optional<VillagePlacement> findByUserVillageIdAndGridXAndGridY(Long userVillageId, int gridX, int gridY);
}
