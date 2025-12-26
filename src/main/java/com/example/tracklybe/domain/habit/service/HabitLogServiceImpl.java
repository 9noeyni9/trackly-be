package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitLog;
import com.example.tracklybe.domain.habit.repository.HabitLogRepository;
import com.example.tracklybe.domain.habit.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HabitLogServiceImpl implements HabitLogService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    @Override
    public GetHabitLogResponse getHabitLogByDate(Long habitId, LocalDate date) {

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new IllegalArgumentException("습관이 존재하지 않습니다."));

        HabitLog habitLog = habitLogRepository.findByHabitAndDate(habit, date);

        return GetHabitLogResponse.from(habitLog);
    }
}
