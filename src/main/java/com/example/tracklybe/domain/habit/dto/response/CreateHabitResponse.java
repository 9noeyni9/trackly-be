package com.example.tracklybe.domain.habit.dto.response;

import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitFrequency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CreateHabitResponse {

    private Long id;
    private String title;
    private String description;
    private HabitFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;

    public CreateHabitResponse(Habit habit) {
        this.id = habit.getId();
        this.title = habit.getTitle();
        this.description = habit.getDescription();
        this.frequency = habit.getHabitFrequency();
        this.startDate = habit.getStartDate();
        this.endDate = habit.getEndDate();
    }
}
