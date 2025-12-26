package com.example.tracklybe.domain.habit.entity;

import com.example.tracklybe.domain.common.entity.Timestamped;
import com.example.tracklybe.domain.habit.dto.request.UpdateHabitRequest;
import com.example.tracklybe.domain.habit.dto.response.GetHabitResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habit extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HabitFrequency habitFrequency;

    private LocalDate startDate;
    private LocalDate endDate;

    public GetHabitResponse toResponse() {
        return GetHabitResponse.builder()
                .habitId(this.id)
                .title(this.title)
                .description(this.description)
                .habitFrequency(this.habitFrequency)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }

    public void update(UpdateHabitRequest updateHabitRequest) {
        this.title = updateHabitRequest.getTitle();
        this.description = updateHabitRequest.getDescription();
        this.habitFrequency = updateHabitRequest.getHabitFrequency();
        this.startDate = updateHabitRequest.getStartDate();
        this.endDate = updateHabitRequest.getEndDate();
    }
}
