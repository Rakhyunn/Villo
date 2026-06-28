package com.villo.domain.todo.repository;

import com.villo.domain.todo.entity.TodoCompletionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoCompletionImageRepository extends JpaRepository<TodoCompletionImage, Long> {
    List<TodoCompletionImage> findByTodoCompletionIdOrderBySortOrder(Long todoCompletionId);
}
