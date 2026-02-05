package com.example.tracklybe.domain.habit.entity;

import com.example.tracklybe.domain.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long habitTagId;

    @ManyToOne
    private Habit habit;

    @ManyToOne
    private Tag tag;
}
