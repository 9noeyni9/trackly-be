package com.example.tracklybe.domain.habit.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "habit_logs")
public class HabitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long habitLogId;

    private Long habitId;

    private LocalDate date;

    @Column(nullable = false)
    private boolean completed;

    private LocalTime completedAt;

    private String note;
}
