package com.example.tracklybe.global.exception;

import com.example.tracklybe.global.exception.enummeration.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HabitNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public HabitNotFoundException(Long habitId) {
        super("Habit with id: " + habitId);
        this.errorCode = ErrorCode.HABIT_NOT_FOUND;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
