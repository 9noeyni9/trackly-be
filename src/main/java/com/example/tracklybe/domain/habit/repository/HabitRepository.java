package com.example.tracklybe.domain.habit.repository;

import com.example.tracklybe.domain.habit.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    Optional<Habit> findByHabitIdAndOwnerUserId(Long habitId, Long ownerUserId);
    List<Habit> findAllByOwnerUserId(Long ownerUserId);
}
