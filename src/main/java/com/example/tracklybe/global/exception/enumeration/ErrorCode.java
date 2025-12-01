package com.example.tracklybe.global.exception.enumeration;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Habit 관련
    HABIT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 습관을 찾을 수 없습니다."),

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

