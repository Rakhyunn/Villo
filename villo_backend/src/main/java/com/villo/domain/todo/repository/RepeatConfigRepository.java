package com.villo.domain.todo.repository;

import com.villo.domain.todo.entity.RepeatConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepeatConfigRepository extends JpaRepository<RepeatConfig, Long> {
    Optional<RepeatConfig> findByTodoId(Long todoId);

    // 자정에 실행할 반복 투두 조회
    List<RepeatConfig> findByNextRunDateBefore(LocalDateTime dateTime);
}
