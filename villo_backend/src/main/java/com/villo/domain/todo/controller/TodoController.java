package com.villo.domain.todo.controller;

import com.villo.domain.todo.dto.*;
import com.villo.domain.todo.service.TodoCompletionService;
import com.villo.domain.todo.service.TodoService;
import com.villo.global.response.ApiResponse;
import com.villo.global.s3.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Todo", description = "투두(퀘스트) — 조회·등록·수정·삭제, AI 분석, 완료·사진 인증")
@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    private final S3Service s3Service;
    private final TodoCompletionService todoCompletionService;

    // 오늘의 투두 목록 조회
    @Operation(summary = "오늘의 퀘스트 목록 조회", description = "오늘 표시할 투두 목록 조회")
    @GetMapping
    public ApiResponse<List<TodoResponse>> getTodayTodos(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok("오늘의 퀘스트 목록입니다.", todoService.getTodayTodos(userId));
    }

    // AI 분석
    @Operation(summary = "투두 AI 분석", description = "제목으로 카테고리·난이도·보상 골드를 분석 (저장 안 함)")
    @PostMapping("/analyze")
    public ApiResponse<TodoAiResultResponse> analyzeTodo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TodoAnalyzeRequest request
    ) {
        return ApiResponse.ok("AI 분석이 완료되었습니다.", todoService.analyzeTodo(request.title()));
    }

    // 투두 등록
    @Operation(summary = "투두 등록", description = "퀘스트 등록 (반복 설정 포함 가능)")
    @PostMapping
    public ApiResponse<TodoResponse> createTodo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TodoCreateRequest request
    ) {
        return ApiResponse.ok("퀘스트가 등록되었습니다.", todoService.createTodo(userId, request));
    }

    // 투두 수정
    @Operation(summary = "투두 수정", description = "제목 변경 시 서버가 AI 재분석 후 저장")
    @PutMapping("/{todoId}")
    public ApiResponse<TodoResponse> updateTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoUpdateRequest request
    ) {
        return ApiResponse.ok("퀘스트가 수정되었습니다.", todoService.updateTodo(userId, todoId, request));
    }

    // 투두 삭제
    @Operation(summary = "투두 삭제", description = "소프트 딜리트 (CANCELLED 처리)")
    @DeleteMapping("/{todoId}")
    public ApiResponse<Void> deleteTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId
    ) {
        todoService.deleteTodo(userId, todoId);
        return ApiResponse.ok("퀘스트가 삭제되었습니다.");
    }

    // 투두 완료 처리
    @Operation(summary = "투두 일반 완료", description = "사진 없이 완료 처리 후 골드 지급")
    @PostMapping("/{todoId}/complete")
    public ApiResponse<TodoCompletionResponse> completeTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId
    ) {
        return ApiResponse.ok("퀘스트를 완료했습니다.", todoCompletionService.completeTodo(userId, todoId));
    }

    // 사진 업로드용 Presigned URL 발급
    @Operation(summary = "사진 업로드 Presigned URL 발급", description = "R2에 직접 업로드할 URL 발급")
    @PostMapping("/images/presigned-url")
    public ApiResponse<PresignedUrlResponse> getPresignedUrl(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        var result = s3Service.generatePresignedUrl(request.fileName());
        return ApiResponse.ok("업로드 URL이 발급되었습니다.",
                new PresignedUrlResponse(result.uploadUrl(), result.imageUrl()));
    }

    // 사진 인증 완료 처리
    @Operation(summary = "투두 사진 인증 완료", description = "인증 사진 등록 후 완료 처리 (골드 +30% 보너스)")
    @PostMapping("/{todoId}/certify")
    public ApiResponse<TodoCertifyResponse> certifyTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoCertifyRequest request
    ) {
        return ApiResponse.ok("퀘스트 인증이 완료되었습니다.",
                todoCompletionService.certifyTodo(userId, todoId, request));
    }
}
