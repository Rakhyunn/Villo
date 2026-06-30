package com.villo.domain.todo.service;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.todo.dto.TodoCompletionResponse;
import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.entity.TodoCompletion;
import com.villo.domain.todo.entity.type.DailyGoldLimit;
import com.villo.domain.todo.entity.type.TodoStatus;
import com.villo.domain.todo.repository.TodoCompletionRepository;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.repository.UserVillageRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class TodoCompletionService {
    private final UserRepository userRepository;
    private final UserVillageRepository userVillageRepository;
    private final TodoCompletionRepository todoCompletionRepository;
    private final TodoService todoService;

    // 투두 일반 완료 처리
    @Transactional
    public TodoCompletionResponse completeTodo(Long userId, Long todoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Todo todo = todoService.getTodoByIdAndUserId(todoId, user.getId());

        // 투두 상태 체크
        if (todo.getStatus() != TodoStatus.PENDING) {
            throw new CustomException(
                    todo.getStatus() == TodoStatus.COMPLETED
                            ? ErrorCode.ALREADY_COMPLETED
                            : ErrorCode.TODO_ALREADY_CANCELLED
            );
        }

        // 일일 골드 초기화 체크 (날짜가 바뀌었으면 초기화)
        if (!user.getLastGoldResetDate().isEqual(LocalDate.now())) {
            user.resetDailyGold();
        }

        // 마을 레벨 조회 → 일일 골드 상한 계산
        UserVillage village = userVillageRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VILLAGE_NOT_FOUND));

        int dailyLimit = DailyGoldLimit.getLimitByLevel(village.getVillageLevel());

        // 실제 지금 골드 계산 (상한 초과 시 잔여분만 지급)
        int earnedGold = calculateEarnedGold(user, todo.getGold(), dailyLimit);

        // 투두 완료 처리
        todo.complete();

        // 골드 적립
        user.increaseGold(earnedGold);

        // 완료 기록 저장
        TodoCompletion completion = TodoCompletion.builder()
                .todo(todo)
                .user(user)
                .isCertified(false)
                .earnedGold(earnedGold)
                .build();

        todoCompletionRepository.save(completion);

        log.info("투두 완료: userId={}, todoId={}, earnedGold={}", userId, todoId, earnedGold);

        return new TodoCompletionResponse(earnedGold, user.getTotalGold(), dailyLimit - user.getDailyGold());
    }

    // 실제 지급 골드 계산
    private int calculateEarnedGold(User user, int todoGold, int dailyLimit) {
        int remaining = dailyLimit - user.getDailyGold();

        if (remaining <= 0) return 0; // 이미 상한 초과

        return Math.min(todoGold, remaining); // 잔여 한도 내에서만 지급
    }
}
