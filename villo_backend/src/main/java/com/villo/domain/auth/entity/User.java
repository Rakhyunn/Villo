package com.villo.domain.auth.entity;

import com.villo.domain.auth.entity.type.Provider;
import com.villo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "nickname"),
                @UniqueConstraint(columnNames = {"provider", "provider_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 50, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(nullable = false)
    private int totalGold = 0;

    @Column(nullable = false)
    private int dailyGold = 0;

    @Column(nullable = false)
    private LocalDate lastGoldResetDate;

    // 골드 적립
    public void increaseGold(int amount) {
        this.totalGold += amount;
        this.dailyGold += amount;
    }

    // 골드 차감
    public void decreaseGold(int amount) {
        this.totalGold -= amount;
    }

    // 일일 골드 초기화
    public void resetDailyGold() {
        this.dailyGold = 0;
        this.lastGoldResetDate = LocalDate.now();
    }

    // 닉네임 변경
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
