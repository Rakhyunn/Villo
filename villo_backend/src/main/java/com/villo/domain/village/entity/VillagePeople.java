package com.villo.domain.village.entity;

import com.villo.domain.village.entity.type.VillagerGrade;
import com.villo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "village_people"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class VillagePeople extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VillagerGrade grade;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private boolean isActive = true; // FALSE면 상점 비노출

    // 상점 비노출 처리
    public void deactivate() {
        this.isActive = false;
    }
}
