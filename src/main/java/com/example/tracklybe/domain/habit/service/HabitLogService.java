package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.HabitLogRequest;
import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.dto.response.HabitLogResponse;

import java.time.LocalDate;

public interface HabitLogService {

    HabitLogResponse toggleToday(Long habitId, HabitLogRequest habitLogRequest);
    GetHabitLogResponse getHabitLogByDate(Long habitId, LocalDate date);
}
