package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.CreateHabitRequest;
import com.example.tracklybe.domain.habit.dto.request.UpdateHabitRequest;
import com.example.tracklybe.domain.habit.dto.response.CreateHabitResponse;
import com.example.tracklybe.domain.habit.dto.response.GetHabitResponse;
import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.repository.HabitRepository;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;


    @Override
    public CreateHabitResponse createHabit(CreateHabitRequest createHabitRequest) {
        Habit habit = Habit.builder()
                .title(createHabitRequest.getTitle())
                .description(createHabitRequest.getDescription())
                .habitFrequency(createHabitRequest.getHabitFrequency())
                .startDate(createHabitRequest.getStartDate())
                .endDate(createHabitRequest.getEndDate())
                .build();
        Habit savedHabit = habitRepository.save(habit);
        return new CreateHabitResponse(savedHabit);
    }

    @Override
    public GetHabitResponse getHabit(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
        return habit.toResponse();
    }

    @Override
    public List<GetHabitResponse> getAllHabits() {
        return habitRepository.findAll().stream()
                .map(Habit::toResponse)
                .toList();
    }

    @Override
    public GetHabitResponse updateHabit(UpdateHabitRequest updateHabitRequest, Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
        habit.update(updateHabitRequest);
        return habit.toResponse();
    }

    @Override
    public void deleteHabit(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
        habitRepository.delete(habit);
    }
}
