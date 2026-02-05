package com.example.tracklybe.domain.habit.repository;

import com.example.tracklybe.domain.habit.entity.HabitTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitTagRepository extends JpaRepository<HabitTag, Long> {
}
