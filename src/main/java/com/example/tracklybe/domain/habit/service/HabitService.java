package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.CreateHabitRequest;
import com.example.tracklybe.domain.habit.dto.response.CreateHabitResponse;

public interface HabitService {

    CreateHabitResponse createHabit(CreateHabitRequest createHabitRequest);
}
