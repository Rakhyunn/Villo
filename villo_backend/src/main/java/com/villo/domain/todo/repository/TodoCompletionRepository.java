package com.villo.domain.todo.repository;

import com.villo.domain.todo.entity.TodoCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodoCompletionRepository extends JpaRepository<TodoCompletion, Long> {
    Optional<TodoCompletion> findByTodoId(Long todoId);
}
