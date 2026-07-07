package com.villo.domain.todo.service;

import com.villo.domain.todo.dto.RepeatConfigRequest;
import com.villo.domain.todo.dto.RepeatConfigResponse;
import com.villo.domain.todo.entity.RepeatConfig;
import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.repository.RepeatConfigRepository;
import com.villo.domain.todo.repository.TodoRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepeatConfigService {
    private final RepeatConfigRepository repeatConfigRepository;
    private final TodoRepository todoRepository;

    // 반복 설정 등록
    @Transactional
    public RepeatConfigResponse createRepeatConfigInternal(
            Long userId,
            Long todoId,
            RepeatConfigRequest request
    ) {
        Todo todo = getTodoByIdAndUserId(todoId, userId);

        // 이미 반복 설정이 있으면 예외
        if (repeatConfigRepository.findByTodoId(todoId).isPresent()) {
            throw new CustomException(ErrorCode.REPEAT_ALREADY_EXISTS);
        }

        RepeatConfig repeatConfig = buildRepeatConfig(todo, request);

        todo.markAsRepeat();

        return RepeatConfigResponse.from(repeatConfigRepository.save(repeatConfig));
    }

    // 투두 등록 시 반복 설정
    public void createRepeatConfigInternal(Todo todo, RepeatConfigRequest request) {
        RepeatConfig config = buildRepeatConfig(todo, request);
        repeatConfigRepository.save(config);
    }

    // 반복 설정 수정
    @Transactional
    public RepeatConfigResponse updateRepeatConfig(Long userId, Long todoId, RepeatConfigRequest request) {
        getTodoByIdAndUserId(todoId, userId); // 본인 확인

        RepeatConfig config = repeatConfigRepository.findByTodoId(todoId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPEAT_CONFIG_NOT_FOUND));

        config.update(
                request.repeatType(),
                request.repeatValue(),
                request.endDate(),
                calculateNextRunDate(request.repeatType(), request.repeatValue())
        );

        return RepeatConfigResponse.from(config);
    }

    // 반복 설정 삭제
    @Transactional
    public void deleteRepeatConfig(Long userId, Long todoId) {
        Todo todo = getTodoByIdAndUserId(todoId, userId);

        RepeatConfig config = repeatConfigRepository.findByTodoId(todoId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPEAT_CONFIG_NOT_FOUND));

        repeatConfigRepository.delete(config);
        todo.unmarkAsRepeat();
    }

    // 본인 투두 확인
    private Todo getTodoByIdAndUserId(Long todoId, Long userId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new CustomException(ErrorCode.TODO_NOT_FOUND));

        if (!todo.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return todo;
    }

    // 다음 실행 시각 계산 (다음 날 자정 기준)
    private LocalDateTime calculateNextRunDate(com.villo.domain.todo.entity.type.RepeatType type, String value) {
        return LocalDateTime.now().toLocalDate().plusDays(1).atStartOfDay();
    }

    // RepeatConfig 빌드
    private RepeatConfig buildRepeatConfig(Todo todo, RepeatConfigRequest request) {
        return RepeatConfig.builder()
                .todo(todo)
                .repeatType(request.repeatType())
                .repeatValue(request.repeatValue())
                .endDate(request.endDate())
                .nextRunDate(calculateNextRunDate(request.repeatType(), request.repeatValue()))
                .build();
    }
}
