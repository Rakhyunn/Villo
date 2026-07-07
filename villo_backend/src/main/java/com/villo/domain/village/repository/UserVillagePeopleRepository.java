package com.villo.domain.village.repository;

import com.villo.domain.village.entity.UserVillagePeople;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVillagePeopleRepository extends JpaRepository<UserVillagePeople, Long> {
    // 유저 보유 주민 목록
    List<UserVillagePeople> findByUserId(Long userId);

    // 보유 주민 수 (마을 레벨 확장 기준)
    int countByUserId(Long userId);
}
