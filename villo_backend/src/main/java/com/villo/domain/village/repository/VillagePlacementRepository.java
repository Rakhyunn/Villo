package com.villo.domain.village.repository;

import com.villo.domain.village.entity.VillagePlacement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VillagePlacementRepository extends JpaRepository<VillagePlacement, Long> {
    // 마을 배치 현황 조회
    List<VillagePlacement> findByUserVillageId(Long userVillageId);
}
