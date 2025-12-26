package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;

import java.time.LocalDate;

public interface HabitLogService {

    GetHabitLogResponse getHabitLogByDate(Long habitId, LocalDate date);
}
