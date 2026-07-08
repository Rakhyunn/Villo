package com.villo.domain.todo.controller;

import com.villo.domain.todo.dto.RepeatConfigRequest;
import com.villo.domain.todo.dto.RepeatConfigResponse;
import com.villo.domain.todo.service.RepeatConfigService;
import com.villo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Repeat", description = "투두 반복 설정 (매일/매주/매월)")
@RestController
@RequestMapping("/api/v1/todos/{todoId}/repeat")
@RequiredArgsConstructor
public class RepeatConfigController {
    private final RepeatConfigService repeatConfigService;

    @Operation(summary = "반복 설정 등록")
    @PostMapping
    public ApiResponse<RepeatConfigResponse> createRepeatConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody RepeatConfigRequest request
    ) {
        return ApiResponse.ok("반복 설정이 등록되었습니다.",
                repeatConfigService.createRepeatConfigInternal(userId, todoId, request));
    }

    @Operation(summary = "반복 설정 수정")
    @PutMapping
    public ApiResponse<RepeatConfigResponse> updateRepeatConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody RepeatConfigRequest request
    ) {
        return ApiResponse.ok("반복 설정이 수정되었습니다.",
                repeatConfigService.updateRepeatConfig(userId, todoId, request));
    }

    @Operation(summary = "반복 설정 삭제")
    @DeleteMapping
    public ApiResponse<Void> deleteRepeatConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId
    ) {
        repeatConfigService.deleteRepeatConfig(userId, todoId);
        return ApiResponse.ok("반복 설정이 삭제되었습니다.");
    }
}
