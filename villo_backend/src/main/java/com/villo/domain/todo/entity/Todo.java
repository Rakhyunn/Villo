package com.villo.domain.todo.entity;

import com.villo.domain.auth.entity.User;
import com.villo.domain.todo.entity.type.Difficulty;
import com.villo.domain.todo.entity.type.TodoStatus;
import com.villo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "todo"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Todo extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_todo_id")
    private Todo parentTodo;    // 반복 생성 시 원본 Todo

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column
    private Difficulty difficulty;

    @Builder.Default
    @Column(nullable = false)
    private int gold = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean isRepeat = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status = TodoStatus.PENDING;

    // 완료 처리
    public void complete() {
        this.status = TodoStatus.COMPLETED;
    }

    // 취소 처리
    public void cancel() {
        this.status = TodoStatus.CANCELLED;
    }

    // AI 분석 결과 업데이트
    public void updateAiResult(String category, Difficulty difficulty, int gold) {
        this.category = category;
        this.difficulty = difficulty;
        this.gold = gold;
    }

    // 제목 수정
    public void updateTitle(String title) {
        this.title = title;
    }

    // 반복 투두로 설정
    public void markAsRepeat() {
        this.isRepeat = true;
    }

    // 반복 투두 해제
    public void unmarkAsRepeat() {
        this.isRepeat = false;
    }
}
