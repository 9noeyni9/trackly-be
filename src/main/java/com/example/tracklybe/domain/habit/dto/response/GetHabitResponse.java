package com.example.tracklybe.domain.habit.dto.response;

import com.example.tracklybe.domain.habit.entity.HabitFrequency;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GetHabitResponse {

    private Long habitId;
    private String title;
    private String description;
    private HabitFrequency habitFrequency;
    private LocalDate startDate;
    private LocalDate endDate;
}
