package com.example.tracklybe.domain.habit.dto.response;

import com.example.tracklybe.domain.habit.entity.HabitLog;

import java.util.List;

public record GetHabitLogListResponse(
        Long habitId,
        List<GetHabitLogResponse> habitLogList
) {
    public static GetHabitLogListResponse of(Long habitId, List<HabitLog> habitLogList) {
        return new GetHabitLogListResponse(
                habitId,
                habitLogList.stream().map(GetHabitLogResponse::from).toList()
        );
    }
}
