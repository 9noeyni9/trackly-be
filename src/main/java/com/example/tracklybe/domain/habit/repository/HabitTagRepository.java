package com.example.tracklybe.domain.habit.repository;

import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitTag;
import com.example.tracklybe.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface HabitTagRepository extends JpaRepository<HabitTag, Long> {

    void deleteByHabitAndTag(Habit habit, Tag tag);
    Set<String> findTagNamesByHabitId(Long habitId);
}
