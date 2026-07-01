package com.villo.domain.todo.repository;

import com.villo.domain.todo.entity.TodoCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoCompletionRepository extends JpaRepository<TodoCompletion, Long> {
    Optional<TodoCompletion> findByTodoId(Long todoId);

    int countByUserId(Long userId);

    // 특정 날짜 완료 투두 조회
    List<TodoCompletion> findByUserIdAndCompletedDateBetween(
            Long userId, LocalDateTime start, LocalDateTime end
    );

    // 특정 월 완료 날짜 목록 조회
    @Query("SELECT DISTINCT CAST(tc.completedDate AS LocalDate) FROM TodoCompletion tc " +
            "WHERE tc.user.id = :userId " +
            "AND YEAR(tc.completedDate) = :year " +
            "AND MONTH(tc.completedDate) = :month")
    List<LocalDate> findCompletedDatesByYearAndMonth(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );
}
