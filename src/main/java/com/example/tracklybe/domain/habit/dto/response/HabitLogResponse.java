package com.example.tracklybe.domain.habit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitLogResponse {

    private Long habitId;
    private String title;
    private boolean completed;
    private String note;
    private LocalTime completedAt;
}
