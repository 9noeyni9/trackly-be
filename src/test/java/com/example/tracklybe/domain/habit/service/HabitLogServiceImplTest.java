package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.HabitLogRequest;
import com.example.tracklybe.domain.habit.dto.response.GetHabitLogResponse;
import com.example.tracklybe.domain.habit.dto.response.HabitLogResponse;
import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitFrequency;
import com.example.tracklybe.domain.habit.entity.HabitLog;
import com.example.tracklybe.domain.habit.repository.HabitLogRepository;
import com.example.tracklybe.domain.habit.repository.HabitRepository;
import com.example.tracklybe.global.exception.HabitLogNotFoundException;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import com.example.tracklybe.global.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitLogServiceImplTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitLogRepository habitLogRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private HabitLogServiceImpl habitLogService;

    @Test
    void toggleToday_updatesExistingLogAndReturnsResponse() {
        Habit habit = habit(1L, "Run");
        HabitLog existingLog = habitLog(10L, habit, LocalDate.of(2026, 3, 30), false, null, null);
        HabitLogRequest request = new HabitLogRequest(true, "done");

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(1L, 100L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.findByHabitAndDate(any(Habit.class), any(LocalDate.class))).thenReturn(Optional.of(existingLog));
        when(habitLogRepository.save(existingLog)).thenReturn(existingLog);

        HabitLogResponse response = habitLogService.toggleToday(1L, request);

        assertThat(response.getHabitId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Run");
        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getNote()).isEqualTo("done");
        assertThat(response.getCompletedAt()).isNotNull();

        verify(habitLogRepository).save(existingLog);
    }

    @Test
    void toggleToday_createsNewLogWhenMissing() {
        Habit habit = habit(2L, "Read");
        HabitLogRequest request = new HabitLogRequest(false, "later");

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(2L, 100L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.findByHabitAndDate(any(Habit.class), any(LocalDate.class))).thenReturn(Optional.empty());
        when(habitLogRepository.save(any(HabitLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HabitLogResponse response = habitLogService.toggleToday(2L, request);

        assertThat(response.getHabitId()).isEqualTo(2L);
        assertThat(response.isCompleted()).isFalse();
        assertThat(response.getCompletedAt()).isNull();
        assertThat(response.getNote()).isEqualTo("later");

        verify(habitLogRepository).save(any(HabitLog.class));
    }

    @Test
    void toggleToday_throwsHabitNotFoundException_whenHabitMissing() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(99L, 100L)).thenReturn(Optional.empty());
        HabitLogRequest request = new HabitLogRequest(true, null);

        assertThatThrownBy(() -> habitLogService.toggleToday(99L, request))
                .isInstanceOf(HabitNotFoundException.class)
                .hasMessageContaining("Habit with id: 99");

        verify(habitLogRepository, never()).findByHabitAndDate(any(Habit.class), any(LocalDate.class));
    }

    @Test
    void getHabitLogByDate_returnsResponse_whenLogExists() {
        Habit habit = habit(3L, "Stretch");
        LocalDate date = LocalDate.of(2026, 3, 30);
        HabitLog log = habitLog(30L, habit, date, true, LocalTime.of(8, 30), "ok");

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(3L, 100L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.findByHabitAndDate(habit, date)).thenReturn(Optional.of(log));

        GetHabitLogResponse response = habitLogService.getHabitLogByDate(3L, date);

        assertThat(response.habitLogId()).isEqualTo(30L);
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.completed()).isTrue();
        assertThat(response.completedAt()).isEqualTo(LocalTime.of(8, 30));
        assertThat(response.note()).isEqualTo("ok");
    }

    @Test
    void getHabitLogByDate_throwsHabitNotFoundException_whenHabitMissing() {
        LocalDate date = LocalDate.of(2026, 3, 30);
        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(404L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitLogService.getHabitLogByDate(404L, date))
                .isInstanceOf(HabitNotFoundException.class);
    }

    @Test
    void getHabitLogByDate_throwsHabitLogNotFoundException_whenLogMissing() {
        Habit habit = habit(4L, "Meditate");
        LocalDate date = LocalDate.of(2026, 3, 30);

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(4L, 100L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.findByHabitAndDate(habit, date)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitLogService.getHabitLogByDate(4L, date))
                .isInstanceOf(HabitLogNotFoundException.class)
                .hasMessageContaining("habitId=4")
                .hasMessageContaining("date=2026-03-30");
    }

    @Test
    void getAllHabitLogs_returnsMappedResponses() {
        Habit firstHabit = habit(5L, "Run");
        Habit secondHabit = habit(6L, "Read");

        HabitLog firstLog = habitLog(50L, firstHabit, LocalDate.of(2026, 3, 29), true, LocalTime.of(6, 0), "good");
        HabitLog secondLog = habitLog(60L, secondHabit, LocalDate.of(2026, 3, 29), false, null, null);

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitLogRepository.findAllByOwnerUserId(100L)).thenReturn(List.of(firstLog, secondLog));

        List<HabitLogResponse> result = habitLogService.getAllHabitLogs();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getHabitId()).isEqualTo(5L);
        assertThat(result.get(0).isCompleted()).isTrue();
        assertThat(result.get(1).getHabitId()).isEqualTo(6L);
        assertThat(result.get(1).isCompleted()).isFalse();
    }

    @Test
    void deleteHabitLogByDate_deletesLog_whenExists() {
        Habit habit = habit(7L, "Study");
        LocalDate date = LocalDate.of(2026, 3, 28);
        HabitLog log = habitLog(70L, habit, date, true, LocalTime.of(22, 0), null);

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(7L, 100L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.findByHabitAndDate(habit, date)).thenReturn(Optional.of(log));

        habitLogService.deleteHabitLogByDate(7L, date);

        verify(habitLogRepository).delete(log);
    }

    @Test
    void deleteHabitLogByDate_throwsHabitLogNotFoundException_whenMissing() {
        Habit habit = habit(8L, "Journal");
        LocalDate date = LocalDate.of(2026, 3, 28);

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(8L, 100L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.findByHabitAndDate(habit, date)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitLogService.deleteHabitLogByDate(8L, date))
                .isInstanceOf(HabitLogNotFoundException.class);
    }

    private Habit habit(Long id, String title) {
        return Habit.builder()
                .habitId(id)
                .title(title)
                .habitFrequency(HabitFrequency.DAILY)
                .build();
    }

    private HabitLog habitLog(Long id, Habit habit, LocalDate date, boolean completed, LocalTime completedAt, String note) {
        return HabitLog.builder()
                .habitLogId(id)
                .habit(habit)
                .date(date)
                .completed(completed)
                .completedAt(completedAt)
                .note(note)
                .build();
    }
}
