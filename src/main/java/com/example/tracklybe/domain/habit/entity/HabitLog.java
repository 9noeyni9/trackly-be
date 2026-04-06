package com.example.tracklybe.domain.habit.entity;

import com.example.tracklybe.domain.common.entity.Timestamped;
import com.example.tracklybe.domain.habit.dto.request.HabitLogRequest;
import com.example.tracklybe.domain.habit.dto.response.HabitLogResponse;
import com.example.tracklybe.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "habit_logs",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"habit_id", "date"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitLog extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long habitLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private LocalDate date;

    @Column(nullable = false)
    private boolean completed;

    private LocalTime completedAt;

    private String note;

    public HabitLogResponse toResponse() {
        return HabitLogResponse.builder()
                .habitId(this.habit.getHabitId())
                .title(this.habit.getTitle())
                .completed(this.completed)
                .note(this.note)
                .completedAt(this.completedAt)
                .build();
    }

    public static HabitLog create(Habit habit, LocalDate today) {
        if (habit == null) throw new IllegalArgumentException("habit is null");
        if (today == null) throw new IllegalArgumentException("today is null");

        return HabitLog.builder()
                .habit(habit)
                .owner(habit.getOwner())
                .date(today)
                .completed(false)
                .completedAt(null)
                .note(null)
                .build();
    }

    public void update(HabitLogRequest habitLogRequest) {
        this.completed = habitLogRequest.isCompleted();

        if (completed) {
            if (this.completedAt == null) {
                this.completedAt = LocalTime.now();
            }
        } else {
            this.completedAt = null;
        }

        this.note = habitLogRequest.getNote();
    }
}
