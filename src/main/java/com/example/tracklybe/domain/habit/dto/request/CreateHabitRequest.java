package com.example.tracklybe.domain.habit.dto.request;

import com.example.tracklybe.domain.habit.entity.HabitFrequency;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateHabitRequest {

    private String title;
    private String description;
    private HabitFrequency habitFrequency;
    private LocalDate startDate;
    private LocalDate endDate;
}
