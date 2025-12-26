package com.example.tracklybe.global.exception;

import com.example.tracklybe.global.exception.enumeration.ErrorCode;

import java.time.LocalDate;

public class HabitLogNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public HabitLogNotFoundException(Long habitId, LocalDate date) {
        super("HabitLog not found. habitId=" + habitId + ", date=" + date);
        this.errorCode = ErrorCode.HABITLOG_NOT_FOUND;
    }

    public ErrorCode getErrorCode() { return  errorCode;}
}
