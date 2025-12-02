package com.example.tracklybe.domain.habit.repository;

import com.example.tracklybe.domain.habit.entity.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    Optional<HabitLog> findByHabitIdAndDate(Long habitId, LocalDate today);
}
