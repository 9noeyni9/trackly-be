package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.HabitLogRequest;
import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.dto.response.HabitLogResponse;
import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitLog;
import com.example.tracklybe.domain.habit.repository.HabitLogRepository;
import com.example.tracklybe.domain.habit.repository.HabitRepository;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitLogServiceImpl implements HabitLogService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    @Override
    public HabitLogResponse toggleToday(Long habitId, HabitLogRequest habitLogRequest) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        LocalDate today = LocalDate.now();

        HabitLog habitLog = habitLogRepository.findByHabitAndDate(habit, today)
                .orElseGet(() -> HabitLog.create(habit, today));

        habitLog.update(habitLogRequest);

        return habitLogRepository.save(habitLog).toResponse();
    }

    @Override
    public GetHabitLogResponse getHabitLogByDate(Long habitId, LocalDate date) {

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new IllegalArgumentException("습관이 존재하지 않습니다."));

        HabitLog habitLog = habitLogRepository.findByHabitAndDate(habit, date)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        return GetHabitLogResponse.from(habitLog);
    }

    @Override
    public List<HabitLogResponse> getAllHabitLogs() {
        return habitLogRepository.findAll().stream()
                .map(HabitLog::toResponse)
                .toList();
    }
}
