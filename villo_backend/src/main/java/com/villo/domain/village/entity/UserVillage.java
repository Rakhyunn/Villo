package com.villo.domain.village.entity;

import com.villo.domain.auth.entity.User;
import com.villo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_village"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class UserVillage extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String villageName;

    @Builder.Default
    @Column(nullable = false)
    private int villageLevel = 1;

    // 마을 이름 변경
    public void updateVillageName(String villageName) {
        this.villageName = villageName;
    }

    // 마을 레벨 업 (주민 수 기준)
    public void levelUp() {
        this.villageLevel++;
    }
}
