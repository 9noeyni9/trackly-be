package com.example.tracklybe.global.exception.handler;

import com.example.tracklybe.domain.common.dto.ApiError;
import com.example.tracklybe.domain.common.dto.ApiResponse;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HabitNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleHabitNotFoundException(HabitNotFoundException e) {
        return buildErrorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        // 내부 메시지 노출 싫으면 detail을 고정 문구로 두는 것도 OK
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "예기치 않은 오류가 발생했습니다.");
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode, String detailMessage) {
        ApiError apiError = ApiError.of(errorCode, detailMessage);
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(apiError));
    }
}
