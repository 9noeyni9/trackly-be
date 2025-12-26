package com.example.tracklybe.domain.habit.controller;

import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.service.HabitLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/habits/{habitId}/logs")
@RequiredArgsConstructor
public class HabitLogController {

    private final HabitLogService habitLogService;

    @GetMapping("/{date}")
    public ResponseEntity<GetHabitLogResponse> getHabitLogByDate(
            @PathVariable Long habitId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(habitLogService.getHabitLogByDate(habitId, date));
    }
}
