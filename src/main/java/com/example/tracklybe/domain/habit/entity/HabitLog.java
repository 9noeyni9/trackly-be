package com.example.tracklybe.domain.habit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "habit_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long habitLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habitId;

    private LocalDate date;

    @Column(nullable = false)
    private boolean completed;

    private LocalTime completedAt;

    private String note;
}
