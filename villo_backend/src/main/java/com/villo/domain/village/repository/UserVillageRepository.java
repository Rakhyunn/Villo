package com.villo.domain.village.repository;

import com.villo.domain.village.entity.UserVillage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVillageRepository extends JpaRepository<UserVillage, Long> {
    Optional<UserVillage> findByUserId(Long userId);
}
