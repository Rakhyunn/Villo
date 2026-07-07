package com.villo.domain.todo.service;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.todo.dto.TodoCertifyRequest;
import com.villo.domain.todo.dto.TodoCertifyResponse;
import com.villo.domain.todo.dto.TodoCompletionResponse;
import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.entity.TodoCompletion;
import com.villo.domain.todo.entity.TodoCompletionImage;
import com.villo.domain.todo.entity.type.DailyGoldLimit;
import com.villo.domain.todo.entity.type.TodoStatus;
import com.villo.domain.todo.repository.TodoCompletionImageRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TodoCompletionService {
    private final UserRepository userRepository;
    private final UserVillageRepository userVillageRepository;
    private final TodoCompletionRepository todoCompletionRepository;
    private final TodoService todoService;
    private final TodoCompletionImageRepository todoCompletionImageRepository;

    // 투두 일반 완료 처리
    @Transactional
    public TodoCompletionResponse completeTodo(Long userId, Long todoId) {
        CompletionContext completionContext = prepareCompletion(userId, todoId);

        // 실제 지금 골드 계산 (상한 초과 시 잔여분만 지급)
        int earnedGold = calculateEarnedGold(completionContext.user(), completionContext.todo().getGold(), completionContext.dailyLimit());

        // 투두 완료 처리
        completionContext.todo().complete();

        // 골드 적립
        completionContext.user().increaseGold(earnedGold);

        // 완료 기록 저장
        TodoCompletion completion = TodoCompletion.builder()
                .todo(completionContext.todo())
                .user(completionContext.user())
                .isCertified(false)
                .earnedGold(earnedGold)
                .build();

        todoCompletionRepository.save(completion);

        log.info("투두 완료: userId={}, todoId={}, earnedGold={}", userId, todoId, earnedGold);

        return new TodoCompletionResponse(
                earnedGold, completionContext.user().getTotalGold(), completionContext.dailyLimit() - completionContext.user().getDailyGold()
        );
    }

    // 사진 인증 완료 처리
    @Transactional
    public TodoCertifyResponse certifyTodo(Long userId, Long todoId, TodoCertifyRequest request) {
        CompletionContext completionContext = prepareCompletion(userId, todoId);

        // 인증 보너스 130% 적용한 골드 계산
        int boostedGold = (int) (completionContext.todo().getGold() * 1.3);
        int earnedGold = calculateEarnedGold(completionContext.user(), boostedGold, completionContext.dailyLimit());

        // 투두 완료 처리
        completionContext.todo().complete();

        // 골드 적립
        completionContext.user().increaseGold(earnedGold);

        // 완료 기록 저장 (인증 완료)
        TodoCompletion completion = TodoCompletion.builder()
                .todo(completionContext.todo())
                .user(completionContext.user())
                .isCertified(true)
                .earnedGold(earnedGold)
                .build();

        TodoCompletion savedCompletion = todoCompletionRepository.save(completion);

        // 인증 사진 저장
        List<TodoCompletionImage> images = new ArrayList<>();
        for (int i = 0; i < request.imageUrls().size(); i++) {
            images.add(TodoCompletionImage.builder()
                    .todoCompletion(savedCompletion)
                    .imageUrl(request.imageUrls().get(i))
                    .sortOrder(i)
                    .build());
        }
        todoCompletionImageRepository.saveAll(images);

        log.info("사진 인증 완료: userId={}, todoId={}, earnedGold={}", userId, todoId, earnedGold);

        return new TodoCertifyResponse(
                earnedGold, completionContext.user().getTotalGold(), completionContext.dailyLimit() - completionContext.user().getDailyGold()
        );
    }

    // 공통 — 완료 처리 전 검증 + 필요한 데이터 준비
    private CompletionContext prepareCompletion(Long userId, Long todoId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Todo todo = todoService.getTodoByIdAndUserId(todoId, userId);

        // 투두 상태 체크
        if (todo.getStatus() != TodoStatus.PENDING) {
            throw new CustomException(
                    todo.getStatus() == TodoStatus.COMPLETED
                            ? ErrorCode.ALREADY_COMPLETED
                            : ErrorCode.TODO_ALREADY_CANCELLED
            );
        }

        // 일일 골드 초기화 체크
        if (!user.getLastGoldResetDate().isEqual(LocalDate.now())) {
            user.resetDailyGold();
        }

        // 마을 레벨 조회 → 일일 골드 상한 계산
        UserVillage village = userVillageRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VILLAGE_NOT_FOUND));

        int dailyLimit = DailyGoldLimit.getLimitByLevel(village.getVillageLevel());

        return new CompletionContext(user, todo, dailyLimit);
    }

    // 실제 지급 골드 계산
    private int calculateEarnedGold(User user, int todoGold, int dailyLimit) {
        int remaining = dailyLimit - user.getDailyGold();

        if (remaining <= 0) return 0; // 이미 상한 초과

        return Math.min(todoGold, remaining); // 잔여 한도 내에서만 지급
    }

    // 유저, 일일 골드 제한 레코드
    private record CompletionContext(User user, Todo todo, int dailyLimit) {}
}
