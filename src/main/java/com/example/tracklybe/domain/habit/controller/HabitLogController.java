package com.example.tracklybe.domain.habit.controller;

import com.example.tracklybe.domain.habit.dto.request.HabitLogRequest;
import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.dto.response.HabitLogResponse;
import com.example.tracklybe.domain.habit.service.HabitLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/habits/{habitId}/logs")
@RequiredArgsConstructor
public class HabitLogController {

    private final HabitLogService habitLogService;

    @PutMapping("/today")
    public ResponseEntity<HabitLogResponse> toggleToday(
            @PathVariable Long habitId,
            @RequestBody HabitLogRequest habitLogRequest
    ) {
        HabitLogResponse habitLogResponse = habitLogService.toggleToday(habitId, habitLogRequest);

        return ResponseEntity.ok().body(habitLogResponse);
    }

    @GetMapping("/{date}")
    public ResponseEntity<GetHabitLogResponse> getHabitLogByDate(
            @PathVariable Long habitId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(habitLogService.getHabitLogByDate(habitId, date));
    }
}
