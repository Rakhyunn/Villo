package com.villo.domain.village.entity;

import com.villo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "village_placement",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_village_id", "grid_x", "grid_y"}),       // 같은 칸 중복 배치 방지
                @UniqueConstraint(columnNames = {"user_village_id", "user_village_people_id"})  // 같은 주민 두 칸 배치 방지
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class VillagePlacement extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_village_id", nullable = false)
    private UserVillage userVillage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_village_people_id", nullable = false)
    private UserVillagePeople userVillagePeople;

    @Column(nullable = false, name = "grid_x")
    private int gridX;

    @Column(nullable = false, name = "grid_y")
    private int gridY;

    // 배치 위치 변경
    public void updatePosition(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }
}
