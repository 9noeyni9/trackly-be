package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.HabitLogRequest;
import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.dto.response.HabitLogResponse;
import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitLog;
import com.example.tracklybe.domain.habit.repository.HabitLogRepository;
import com.example.tracklybe.domain.habit.repository.HabitRepository;
import com.example.tracklybe.global.exception.HabitLogNotFoundException;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
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
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        HabitLog habitLog = habitLogRepository.findByHabitAndDate(habit, date)
                .orElseThrow(() -> new HabitLogNotFoundException(habit.getId(), date));

        return GetHabitLogResponse.from(habitLog);
    }

    @Override
    public List<HabitLogResponse> getAllHabitLogs() {
        return habitLogRepository.findAll().stream()
                .map(HabitLog::toResponse)
                .toList();
    }
}
