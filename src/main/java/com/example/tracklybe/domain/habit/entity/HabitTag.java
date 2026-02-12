package com.example.tracklybe.domain.habit.entity;

import com.example.tracklybe.domain.tag.entity.Tag;
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

@Table(name = "habit_tags", uniqueConstraints = @UniqueConstraint(columnNames = {"habit_id", "tag_id"}))
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long habitTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
