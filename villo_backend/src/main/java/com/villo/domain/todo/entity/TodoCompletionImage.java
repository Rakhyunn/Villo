package com.villo.domain.todo.entity;

import com.villo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "todo_completion_image",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"todo_completion_id", "sort_order"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class TodoCompletionImage extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_completion_id", nullable = false)
    private TodoCompletion todoCompletion;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private int sortOrder = 0;
}
