package com.example.tracklybe.domain.habit.controller;

import com.example.tracklybe.domain.habit.dto.request.HabitLogRequest;
import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.dto.response.HabitLogResponse;
import com.example.tracklybe.domain.habit.service.HabitLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitLogController {

    private final HabitLogService habitLogService;

    @PatchMapping("/{habitId}/logs/today")
    public ResponseEntity<HabitLogResponse> toggleToday(
            @PathVariable Long habitId,
            @Valid @RequestBody HabitLogRequest habitLogRequest
    ) {
        HabitLogResponse habitLogResponse = habitLogService.toggleToday(habitId, habitLogRequest);

        return ResponseEntity.ok().body(habitLogResponse);
    }

    @GetMapping("/{habitId}/logs/{date}")
    public ResponseEntity<GetHabitLogResponse> getHabitLogByDate(
            @PathVariable Long habitId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(habitLogService.getHabitLogByDate(habitId, date));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<HabitLogResponse>> getAllHabitLogs() {
        List<HabitLogResponse> habitLogList = habitLogService.getAllHabitLogs();
        return ResponseEntity.ok().body(habitLogList);
    }

    @DeleteMapping("/{habitId}/logs/{date}")
    public ResponseEntity<Void> deleteHabitLogByDate(
            @PathVariable Long habitId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        habitLogService.deleteHabitLogByDate(habitId, date);
        return ResponseEntity.noContent().build();
    }
}
