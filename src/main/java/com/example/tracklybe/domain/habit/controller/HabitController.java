package com.example.tracklybe.domain.habit.controller;

import com.example.tracklybe.domain.habit.dto.request.CreateHabitRequest;
import com.example.tracklybe.domain.habit.dto.request.UpdateHabitRequest;
import com.example.tracklybe.domain.habit.dto.response.CreateHabitResponse;
import com.example.tracklybe.domain.habit.dto.response.GetHabitResponse;
import com.example.tracklybe.domain.habit.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/habit")
public class HabitController {

    private final HabitService habitService;

    @PostMapping
    public ResponseEntity<CreateHabitResponse> createHabit(@Valid @RequestBody CreateHabitRequest createHabitRequest) {
        CreateHabitResponse createHabitResponse = habitService.createHabit(createHabitRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createHabitResponse.getId())
                .toUri();
        return ResponseEntity.created(location).body(createHabitResponse);
    }

    @GetMapping("/{habitId}")
    public ResponseEntity<GetHabitResponse> getHabit(@PathVariable Long habitId) {
        GetHabitResponse getHabitResponse = habitService.getHabit(habitId);
        return ResponseEntity.ok().body(getHabitResponse);
    }

    @GetMapping
    public ResponseEntity<List<GetHabitResponse>> getAllHabits() {
        List<GetHabitResponse> getHabitResponseList = habitService.getAllHabits();
        return ResponseEntity.ok().body(getHabitResponseList);
    }

    @PatchMapping("/{habitId}")
    public ResponseEntity<GetHabitResponse> updateHabit(@Valid @RequestBody UpdateHabitRequest updateHabitRequest,
                                                        @PathVariable Long habitId) {
        GetHabitResponse updateGetHabitResponse = habitService.updateHabit(updateHabitRequest, habitId);
        return ResponseEntity.ok().body(updateGetHabitResponse);
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId) {
        habitService.deleteHabit(habitId);
        return ResponseEntity.noContent().build();
    }
}
