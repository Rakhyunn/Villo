package com.villo.domain.todo.entity;

import com.villo.domain.todo.entity.type.RepeatType;
import com.villo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "repeat_config"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class RepeatConfig extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false, unique = true)
    private Todo todo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepeatType repeatType;

    @Column(length = 50)
    private String repeatValue; // WEEKLY: MON,WED / MONTHLY: 1,15 / DAILY: NULL

    private LocalDate endDate; // NULL이면 무기한

    @Column(nullable = false)
    private LocalDateTime nextRunDate;

    // 다음 실행 시각 업데이트 (Spring Batch)
    public void updateNextRunDate(LocalDateTime nextRunDate) {
        this.nextRunDate = nextRunDate;
    }
}
