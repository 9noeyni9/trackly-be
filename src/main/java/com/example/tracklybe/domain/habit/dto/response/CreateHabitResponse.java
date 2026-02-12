package com.example.tracklybe.domain.habit.dto.response;

import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitFrequency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class CreateHabitResponse {

    private Long id;
    private String title;
    private String description;
    private HabitFrequency habitFrequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> tags;

    public CreateHabitResponse(Habit habit, List<String> tags) {
        this.id = habit.getHabitId();
        this.title = habit.getTitle();
        this.description = habit.getDescription();
        this.habitFrequency = habit.getHabitFrequency();
        this.startDate = habit.getStartDate();
        this.endDate = habit.getEndDate();
        this.tags = tags;
    }
}
