package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.CreateHabitRequest;
import com.example.tracklybe.domain.habit.dto.request.UpdateHabitRequest;
import com.example.tracklybe.domain.habit.dto.response.CreateHabitResponse;
import com.example.tracklybe.domain.habit.dto.response.GetHabitResponse;
import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitTag;
import com.example.tracklybe.domain.habit.repository.HabitRepository;
import com.example.tracklybe.domain.habit.repository.HabitTagRepository;
import com.example.tracklybe.domain.tag.entity.Tag;
import com.example.tracklybe.domain.tag.repository.TagRepository;
import com.example.tracklybe.domain.tag.service.TagService;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;
    private final HabitTagRepository habitTagRepository;
    private final TagRepository tagRepository;
    private final TagService tagService;

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

        List<Tag> tags = tagService.getOrCreateAll(createHabitRequest.getTags());
        if(!tags.isEmpty()) {
            List<HabitTag> links = tags.stream()
                    .map(tag -> HabitTag.builder()
                            .habit(savedHabit)
                            .tag(tag)
                            .build())
                    .toList();
            habitTagRepository.saveAll(links);
        }
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
        updateTags(habitId, updateHabitRequest.getTags());
        return habit.toResponse();
    }

    @Override
    public void deleteHabit(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
        habitRepository.delete(habit);
    }

    @Override
    public void detachTag(Long habitId, String tagName) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        Tag tag = tagRepository.findByName(tagName.trim())
                .orElseThrow(() -> new IllegalArgumentException("tag not found: " + tagName));

        habitTagRepository.deleteByHabitAndTag(habit, tag);
    }

    @Override
    public void updateTags(Long habitId, List<String> requestedTagNames) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        Set<String> newTags = requestedTagNames.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        Set<String> oldTags = habitTagRepository.findTagNamesByHabitId(habitId);

        Set<String> toRemove = new HashSet<>(oldTags);
        toRemove.removeAll(newTags);

        Set<String> toAdd = new HashSet<>(newTags);
        toAdd.removeAll(oldTags);

        if(!toRemove.isEmpty()) {
            Map<String, Tag> existed = tagRepository.findByNameIn(toAdd.stream().toList()).stream()
                    .collect(Collectors.toMap(Tag::getName, t -> t));

            List<HabitTag> newLinlks = new ArrayList<>();
            for(String name : toAdd) {
                Tag tag = existed.get(name);
                if(tag == null) {
                    tag = tagRepository.save(Tag.builder().name(name).build());
                }
                newLinlks.add(HabitTag.builder()
                        .habit(habit)
                        .tag(tag)
                        .build());
            }
            habitTagRepository.saveAll(newLinlks);
        }
    }
}
