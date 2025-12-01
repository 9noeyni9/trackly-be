package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.CreateHabitRequest;
import com.example.tracklybe.domain.habit.dto.response.CreateHabitResponse;
import com.example.tracklybe.domain.habit.dto.response.GetHabitResponse;

import java.util.List;

public interface HabitService {

    CreateHabitResponse createHabit(CreateHabitRequest createHabitRequest);
    GetHabitResponse getHabit(Long habitId);
    List<GetHabitResponse> getAllHabits();
}
