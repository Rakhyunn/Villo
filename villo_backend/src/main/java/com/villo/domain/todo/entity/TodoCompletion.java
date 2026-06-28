package com.villo.domain.todo.entity;

import com.villo.domain.auth.entity.User;
import com.villo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "todo_completion",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "todo_id") // 투두당 1회만 완료
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class TodoCompletion extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false, unique = true)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean isCertified = false;

    @Column(nullable = false)
    private int earnedGold = 0;
}
