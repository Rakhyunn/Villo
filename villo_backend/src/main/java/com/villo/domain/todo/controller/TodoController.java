package com.villo.domain.todo.controller;

import com.villo.domain.todo.dto.TodoCreateRequest;
import com.villo.domain.todo.dto.TodoResponse;
import com.villo.domain.todo.dto.TodoUpdateRequest;
import com.villo.domain.todo.service.TodoService;
import com.villo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;

    // 오늘의 투두 목록 조회
    @GetMapping
    public ApiResponse<List<TodoResponse>> getTodayTodos(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("오늘의 퀘스트 목록입니다.", todoService.getTodayTodos(userId));
    }

    // 투두 등록
    @PostMapping
    public ApiResponse<TodoResponse> createTodo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TodoCreateRequest request
    ) {
        return ApiResponse.ok("퀘스트가 등록되었습니다.", todoService.createTodo(userId, request));
    }

    // 투두 수정
    @PutMapping("/{todoId}")
    public ApiResponse<TodoResponse> updateTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoUpdateRequest request
    ) {
        return ApiResponse.ok("퀘스트가 수정되었습니다.", todoService.updateTodo(userId, todoId, request));
    }

    // 투두 삭제
    @DeleteMapping("/{todoId}")
    public ApiResponse<Void> deleteTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId
    ) {
        todoService.deleteTodo(userId, todoId);
        return ApiResponse.ok("퀘스트가 삭제되었습니다.");
    }
}
