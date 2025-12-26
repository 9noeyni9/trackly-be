package com.example.tracklybe.domain.habit.repository;

import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface HabitLogRepository extends JpaRepository<HabitLog,Long> {

    Optional<HabitLog> findByHabitAndDate(Habit habit, LocalDate date);
}
