package com.example.tracklybe.global.exception.handler;

import com.example.tracklybe.domain.common.dto.ApiError;
import com.example.tracklybe.domain.common.dto.ApiResponse;
import com.example.tracklybe.global.exception.HabitLogNotFoundException;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import com.example.tracklybe.global.exception.InvalidRequestException;
import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HabitNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleHabitNotFoundException(HabitNotFoundException e) {
        return buildErrorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(HabitLogNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleHabitLogNotFoundException(HabitLogNotFoundException e) {
        return buildErrorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestException(InvalidRequestException e) {
        return buildErrorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorMessage)
                .collect(Collectors.joining(", "));
        return buildErrorResponse(ErrorCode.INVALID_REQUEST, detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String detail = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return buildErrorResponse(ErrorCode.INVALID_REQUEST, detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return buildErrorResponse(ErrorCode.INVALID_REQUEST, "요청 본문 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "예기치 않은 오류가 발생했습니다.");
    }

    private String toFieldErrorMessage(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode, String detailMessage) {
        ApiError apiError = ApiError.of(errorCode, detailMessage);
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(apiError));
    }
}
