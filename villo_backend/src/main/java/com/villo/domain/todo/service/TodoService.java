package com.villo.domain.todo.service;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.repository.UserRepository;
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

    // 오늘의 투두 목록 조회
    @Transactional(readOnly = true)
    public List<TodoResponse> getTodayTodos(Long userId) {
        checkUser(userId);

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return todoRepository
                .findByUserIdAndCreatedDateBetween(userId, startOfDay, endOfDay)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    // 투두 등록 (AI 분석은 별도 서비스에서 처리)
    @Transactional
    public TodoResponse createTodo(Long userId, TodoCreateRequest request) {
        User user = checkUser(userId);

        Todo todo = Todo.builder()
                .user(user)
                .title(request.title())
                .isRepeat(request.isRepeat())
                .category("미분류")       // AI 분석 전 임시값
                .difficulty(null)         // AI 분석 후 설정
                .gold(0)                  // AI 분석 후 설정
                .status(TodoStatus.PENDING)
                .build();

        return TodoResponse.from(todoRepository.save(todo));
    }

    // 투두 수정
    @Transactional
    public TodoResponse updateTodo(Long userId, Long todoId, TodoUpdateRequest request) {
        checkUser(userId);

        Todo todo = getTodoByIdAndUserId(todoId, userId);
        todo.updateTitle(request.title());
        return TodoResponse.from(todo);
    }

    // 투두 삭제
    @Transactional
    public void deleteTodo(Long userId, Long todoId) {
        checkUser(userId);

        Todo todo = getTodoByIdAndUserId(todoId, userId);
        todo.cancel(); // 소프트 딜리트 (CANCELLED 처리)
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

    // 유저 존재 여부
    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
