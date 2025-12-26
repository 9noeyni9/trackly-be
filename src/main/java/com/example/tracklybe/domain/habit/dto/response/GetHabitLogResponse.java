package com.example.tracklybe.domain.habit.dto.response;

import com.example.tracklybe.domain.habit.entity.HabitLog;

import java.time.LocalDate;
import java.time.LocalTime;

public record GetHabitLogResponse(
        Long habitLogId,
        LocalDate date,
        boolean completed,
        LocalTime completedAt,
        String note
) {
    public static GetHabitLogResponse from(HabitLog habitLog) {
        return new GetHabitLogResponse(
                habitLog.getHabitLogId(),
                habitLog.getDate(),
                habitLog.isCompleted(),
                habitLog.getCompletedAt(),
                habitLog.getNote()
        );
    }
}
