package com.example.tracklybe.domain.habit.controller;

import com.example.tracklybe.domain.habit.dto.request.CreateHabitRequest;
import com.example.tracklybe.domain.habit.dto.response.CreateHabitResponse;
import com.example.tracklybe.domain.habit.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
}
