package com.villo.domain.todo.repository;

import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.entity.type.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    // 오늘의 투두 목록 조회
    List<Todo> findByUserIdAndCreatedDateBetween(
            Long userId, LocalDateTime start, LocalDateTime end
    );

    // 상태별 조회
    List<Todo> findByUserIdAndStatus(Long userId, TodoStatus status);
}
