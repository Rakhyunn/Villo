package com.villo.domain.todo.controller;

import com.villo.domain.todo.dto.RepeatConfigRequest;
import com.villo.domain.todo.dto.RepeatConfigResponse;
import com.villo.domain.todo.service.RepeatConfigService;
import com.villo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/todos/{todoId}/repeat")
@RequiredArgsConstructor
public class RepeatConfigController {
    private final RepeatConfigService repeatConfigService;

    @PostMapping
    public ApiResponse<RepeatConfigResponse> createRepeatConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody RepeatConfigRequest request
    ) {
        return ApiResponse.ok("반복 설정이 등록되었습니다.",
                repeatConfigService.createRepeatConfigInternal(userId, todoId, request));
    }

    @PutMapping
    public ApiResponse<RepeatConfigResponse> updateRepeatConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody RepeatConfigRequest request
    ) {
        return ApiResponse.ok("반복 설정이 수정되었습니다.",
                repeatConfigService.updateRepeatConfig(userId, todoId, request));
    }

    @DeleteMapping
    public ApiResponse<Void> deleteRepeatConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId
    ) {
        repeatConfigService.deleteRepeatConfig(userId, todoId);
        return ApiResponse.ok("반복 설정이 삭제되었습니다.");
    }
}
