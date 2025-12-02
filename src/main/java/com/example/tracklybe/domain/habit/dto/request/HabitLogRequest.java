package com.example.tracklybe.domain.habit.dto.request;

import lombok.Getter;

@Getter
public class HabitLogRequest {

    private boolean completed;
    private String note;
}
