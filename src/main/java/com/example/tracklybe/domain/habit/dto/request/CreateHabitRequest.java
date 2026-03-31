package com.example.tracklybe.domain.habit.dto.request;

import com.example.tracklybe.domain.habit.entity.HabitFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class CreateHabitRequest {

    @NotBlank(message = "습관명을 입력해주세요.")
    @Size(max = 50, message = "습관명은 50자 이하여야 합니다.")
    private String title;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;

    @NotNull(message = "습관 주기를 입력해주세요.")
    private HabitFrequency habitFrequency;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다.")
    private List<@NotBlank(message = "태그는 공백일 수 없습니다.")
            @Size(max = 30, message = "태그명은 30자 이하여야 합니다.") String> tags;
}
