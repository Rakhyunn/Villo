package com.villo.domain.todo.scheduler;

import com.villo.domain.todo.entity.RepeatConfig;
import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.entity.type.RepeatType;
import com.villo.domain.todo.entity.type.TodoStatus;
import com.villo.domain.todo.repository.RepeatConfigRepository;
import com.villo.domain.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RepeatTodoScheduler {
    private final RepeatConfigRepository repeatConfigRepository;
    private final TodoRepository todoRepository;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void generateRepeatTodos() {
        log.info("반복 투두 자동 생성 시작");

        LocalDateTime now = LocalDateTime.now();
        List<RepeatConfig> targets = repeatConfigRepository.findByNextRunDateBefore(now);

        int successCount = 0;
        int failCount = 0;

        for (RepeatConfig config : targets) {
            try {
                processRepeatConfig(config);
                successCount++;
            } catch (Exception e) {
                // 하나 실패해도 나머지는 계속 처리
                log.error("반복 투두 생성 실패: repeatConfigId={}, error={}",
                        config.getId(), e.getMessage());
                failCount++;
            }
        }

        log.info("반복 투두 자동 생성 완료: 성공={}, 실패={}", successCount, failCount);
    }

    private void processRepeatConfig(RepeatConfig config) {
        // 종료일이 지났으면 스킵
        if (config.getEndDate() != null && config.getEndDate().isBefore(LocalDate.now())) {
            log.info("반복 종료됨: repeatConfigId={}", config.getId());
            return;
        }

        Todo originalTodo = config.getTodo();

        // 오늘이 반복 요일/날짜에 해당하는지 체크
        if (!isTargetDate(config)) {
            // 해당 없는 날이면 다음 실행일만 갱신
            config.updateNextRunDate(calculateNextRunDate(config.getRepeatType()));
            return;
        }

        // 원본 투두를 복사해서 새 투두 생성
        Todo newTodo = Todo.builder()
                .user(originalTodo.getUser())
                .parentTodo(originalTodo)
                .title(originalTodo.getTitle())
                .category(originalTodo.getCategory())
                .difficulty(originalTodo.getDifficulty())
                .gold(originalTodo.getGold())
                .isRepeat(true)
                .status(TodoStatus.PENDING)
                .build();

        todoRepository.save(newTodo);

        // 다음 실행일 갱신
        config.updateNextRunDate(calculateNextRunDate(config.getRepeatType()));

        log.info("반복 투두 생성: originalTodoId={}, newTodoId={}",
                originalTodo.getId(), newTodo.getId());
    }

    // 오늘이 반복 대상 날짜인지 확인
    private boolean isTargetDate(RepeatConfig config) {
        LocalDate today = LocalDate.now();

        return switch (config.getRepeatType()) {
            case DAILY -> true;
            case WEEKLY -> isTargetDayOfWeek(today, config.getRepeatValue());
            case MONTHLY -> isTargetDayOfMonth(today, config.getRepeatValue());
        };
    }

    // 주간 반복 — 오늘 요일이 설정된 요일에 포함되는지
    private boolean isTargetDayOfWeek(LocalDate today, String repeatValue) {
        if (repeatValue == null || repeatValue.isBlank()) return false;

        Set<DayOfWeek> targetDays = Arrays.stream(repeatValue.split(","))
                .map(String::trim)
                .map(this::parseDayOfWeek)
                .collect(Collectors.toSet());

        return targetDays.contains(today.getDayOfWeek());
    }

    private DayOfWeek parseDayOfWeek(String value) {
        return switch (value.toUpperCase()) {
            case "MON" -> DayOfWeek.MONDAY;
            case "TUE" -> DayOfWeek.TUESDAY;
            case "WED" -> DayOfWeek.WEDNESDAY;
            case "THU" -> DayOfWeek.THURSDAY;
            case "FRI" -> DayOfWeek.FRIDAY;
            case "SAT" -> DayOfWeek.SATURDAY;
            case "SUN" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("잘못된 요일 값: " + value);
        };
    }

    // 월간 반복 — 오늘 날짜가 설정된 날짜에 포함되는지 (1~28 + 말일)
    private boolean isTargetDayOfMonth(LocalDate today, String repeatValue) {
        if (repeatValue == null || repeatValue.isBlank()) return false;

        int todayDay = today.getDayOfMonth();
        boolean isLastDay = today.equals(YearMonth.from(today).atEndOfMonth());

        Set<String> targetDays = Arrays.stream(repeatValue.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        // "말일" 설정 + 오늘이 말일인 경우
        if (targetDays.contains("LAST") && isLastDay) return true;

        // 숫자 날짜 매칭
        return targetDays.contains(String.valueOf(todayDay));
    }

    // 다음 실행일 계산 (반복 타입과 무관하게 매일 자정 체크하도록 다음 날로 설정)
    private LocalDateTime calculateNextRunDate(RepeatType type) {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }
}
