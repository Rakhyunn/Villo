package com.villo.domain.village.repository;

import com.villo.domain.village.entity.VillagePeople;
import com.villo.domain.village.entity.type.VillagerGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VillagePeopleRepository extends JpaRepository<VillagePeople, Long> {
    // 활성화된 전체 주민 조회
    List<VillagePeople> findByIsActiveTrue();

    // 등급별 필터 조회
    List<VillagePeople> findByGradeAndIsActiveTrue(VillagerGrade grade);
}
