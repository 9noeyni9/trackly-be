package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.HabitLogRequest;
import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.dto.response.HabitLogResponse;
import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitLog;
import com.example.tracklybe.domain.habit.repository.HabitLogRepository;
import com.example.tracklybe.domain.habit.repository.HabitRepository;
import com.example.tracklybe.global.exception.ForbiddenException;
import com.example.tracklybe.global.exception.HabitLogNotFoundException;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import com.example.tracklybe.global.security.CurrentUserProvider;
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
    private final CurrentUserProvider currentUserProvider;

    @Override
    public HabitLogResponse toggleToday(Long habitId, HabitLogRequest habitLogRequest) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        Habit habit = findOwnedHabitOrThrow(habitId, currentUserId);

        LocalDate today = LocalDate.now();

        HabitLog habitLog = habitLogRepository.findByHabitAndDate(habit, today)
                .orElseGet(() -> HabitLog.create(habit, today));

        habitLog.update(habitLogRequest);

        return habitLogRepository.save(habitLog).toResponse();
    }

    @Override
    public GetHabitLogResponse getHabitLogByDate(Long habitId, LocalDate date) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        Habit habit = findOwnedHabitOrThrow(habitId, currentUserId);

        HabitLog habitLog = habitLogRepository.findByHabitAndDate(habit, date)
                .orElseThrow(() -> new HabitLogNotFoundException(habit.getHabitId(), date));

        return GetHabitLogResponse.from(habitLog);
    }

    @Override
    public List<HabitLogResponse> getAllHabitLogs() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return habitLogRepository.findAllByOwnerUserId(currentUserId).stream()
                .map(HabitLog::toResponse)
                .toList();
    }

    @Override
    public void deleteHabitLogByDate(Long habitId, LocalDate date) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        Habit habit = findOwnedHabitOrThrow(habitId, currentUserId);

        HabitLog habitLog = habitLogRepository.findByHabitAndDate(habit, date)
                .orElseThrow(() -> new HabitLogNotFoundException(habit.getHabitId(), date));

        habitLogRepository.delete(habitLog);
    }

    private Habit findOwnedHabitOrThrow(Long habitId, Long currentUserId) {
        return habitRepository.findByHabitIdAndOwnerUserId(habitId, currentUserId)
                .orElseGet(() -> {
                    if (habitRepository.existsById(habitId)) {
                        throw new ForbiddenException("해당 습관에 접근 권한이 없습니다.");
                    }
                    throw new HabitNotFoundException(habitId);
                });
    }
}
