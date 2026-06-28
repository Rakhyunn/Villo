package com.villo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT("400-1", HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    UNAUTHORIZED("401-1", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN("403-1", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND("404-1", HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("500-1", HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증 (구현하면서 추가 예정)

    // 투두 (구현하면서 추가 예정)

    // 마을 (구현하면서 추가 예정)
    ;

    private final String resultCode;
    private final HttpStatus httpStatus;
    private final String message;
}
