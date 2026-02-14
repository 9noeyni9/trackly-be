package com.example.tracklybe.domain.habit.dto.request;

import com.example.tracklybe.domain.habit.entity.HabitFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class CreateHabitRequest {

    @NotBlank(message = "습관명을 입력해주세요.")
    private String title;
    private String description;
    @NotNull(message = "습관 주기를 입력해주세요.")
    private HabitFrequency habitFrequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> tags;
}
