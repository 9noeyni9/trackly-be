package com.example.tracklybe.global.exception;

import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import lombok.Getter;

@Getter
public class HabitNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public HabitNotFoundException(Long habitId) {
        super("Habit with id: " + habitId);
        this.errorCode = ErrorCode.HABIT_NOT_FOUND;
    }
}
