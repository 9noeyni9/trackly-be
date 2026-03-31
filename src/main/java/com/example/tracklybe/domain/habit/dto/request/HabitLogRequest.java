package com.example.tracklybe.domain.habit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HabitLogRequest {

    private boolean completed;

    @Size(max = 500, message = "노트는 500자 이하여야 합니다.")
    private String note;
}
