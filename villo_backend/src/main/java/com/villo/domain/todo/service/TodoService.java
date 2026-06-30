package com.villo.domain.todo.service;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.todo.dto.TodoAiResultResponse;
import com.villo.domain.todo.dto.TodoCreateRequest;
import com.villo.domain.todo.dto.TodoResponse;
import com.villo.domain.todo.dto.TodoUpdateRequest;
import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.entity.type.TodoStatus;
import com.villo.domain.todo.repository.TodoRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoAiService todoAiService;

    // 오늘의 투두 목록 조회
    @Transactional(readOnly = true)
    public List<TodoResponse> getTodayTodos(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return todoRepository
                .findByUserIdAndCreatedDateBetween(userId, startOfDay, endOfDay)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    // AI 분석만 (DB 저장 안 함)
    public TodoAiResultResponse analyzeTodo(String title) {
        return todoAiService.analyze(title);
    }

    // 투두 등록 (AI 분석 결과 받아서 저장)
    @Transactional
    public TodoResponse createTodo(Long userId, TodoCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Todo todo = Todo.builder()
                .user(user)
                .title(request.title())
                .category(request.category())
                .difficulty(request.difficulty())
                .gold(request.gold())
                .isRepeat(request.isRepeat())
                .status(TodoStatus.PENDING)
                .build();

        return TodoResponse.from(todoRepository.save(todo));
    }

    // 투두 수정 + AI 재분석
    @Transactional
    public TodoResponse updateTodo(Long userId, Long todoId, TodoUpdateRequest request) {
        Todo todo = getTodoByIdAndUserId(todoId, userId);
        // 제목 변경 시 AI 재분석
        if (!todo.getTitle().equals(request.title())) {
            TodoAiResultResponse aiResult = todoAiService.analyze(request.title());
            todo.updateAiResult(aiResult.category(), aiResult.difficulty(), aiResult.gold());
        }
        todo.updateTitle(request.title());
        return TodoResponse.from(todo);
    }

    // 투두 삭제
    @Transactional
    public void deleteTodo(Long userId, Long todoId) {
        Todo todo = getTodoByIdAndUserId(todoId, userId);
        todo.cancel(); // 소프트 딜리트 (CANCELLED 처리)
    }

    // AI 재분석
    @Transactional
    public TodoResponse reanalyzeTodo(Long userId, Long todoId) {
        Todo todo = getTodoByIdAndUserId(todoId, userId);

        // AI 재분석
        TodoAiResultResponse aiResult = todoAiService.analyze(todo.getTitle());

        todo.updateAiResult(aiResult.category(), aiResult.difficulty(), aiResult.gold());

        return TodoResponse.from(todo);
    }

    // 공통 — 투두 조회 + 본인 확인
    public Todo getTodoByIdAndUserId(Long todoId, Long userId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new CustomException(ErrorCode.TODO_NOT_FOUND));

        if (!todo.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return todo;
    }
}
