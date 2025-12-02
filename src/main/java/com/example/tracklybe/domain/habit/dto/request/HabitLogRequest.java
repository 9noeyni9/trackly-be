package com.example.tracklybe.domain.habit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HabitLogRequest {

    private boolean completed;
    private String note;
}
