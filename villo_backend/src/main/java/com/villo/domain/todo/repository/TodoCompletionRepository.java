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
    @Query("""
        select distinct tc from TodoCompletion tc
        join fetch tc.todo
        left join fetch tc.images
        where tc.user.id = :userId
        and tc.completedDate >= :start and tc.completedDate < :end
    """)
    List<TodoCompletion> findAllWithTodoAndImagesByUserIdAndCompletedDateBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
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

    // 최근 N일간의 완료 날짜 목록을 한 번에 조회 (연속 달성일 계산용)
    @Query("""
        select distinct cast(tc.completedDate as LocalDate)
        from TodoCompletion tc
        where tc.user.id = :userId
        and tc.completedDate >= :fromDate
        order by cast(tc.completedDate as LocalDate) desc
    """)
    List<LocalDate> findCompletedDatesSince(@Param("userId") Long userId, @Param("fromDate") LocalDateTime fromDate);
}
