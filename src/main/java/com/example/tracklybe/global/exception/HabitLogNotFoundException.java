package com.example.tracklybe.global.exception;

import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class HabitLogNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public HabitLogNotFoundException(Long habitId, LocalDate date) {
        super("HabitLog not found. habitId=" + habitId + ", date=" + date);
        this.errorCode = ErrorCode.HABITLOG_NOT_FOUND;
    }
}
