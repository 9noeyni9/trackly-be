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
import com.example.tracklybe.domain.tag.service.TagService;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;
    private final HabitTagRepository habitTagRepository;
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

        List<Tag> tags = tagService.getOrCreateEntities(createHabitRequest.getTags());
        if (!tags.isEmpty()) {
            List<HabitTag> links = tags.stream()
                    .map(tag -> HabitTag.builder()
                            .habit(savedHabit)
                            .tag(tag)
                            .build())
                    .toList();
            habitTagRepository.saveAll(links);
        }
        List<String> tagNames = tags.stream().map(Tag::getName).toList();
        return new CreateHabitResponse(savedHabit, tagNames);
    }

    @Override
    public GetHabitResponse getHabit(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
        Set<String> tags = habitTagRepository.findTagNamesByHabitId(habitId);
        return habit.toResponse(tags);
    }

    @Override
    public List<GetHabitResponse> getAllHabits() {
        List<Habit> habits = habitRepository.findAll();
        List<Long> habitIds = habits.stream().map(Habit::getHabitId).toList();
        if (habitIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Set<String>> tagsByHabitId = habitTagRepository.findHabitIdAndTagNameByHabitIds(habitIds).stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0],
                        Collectors.mapping(row -> (String) row[1], Collectors.toSet())
                ));

        return habits.stream()
                .map(h -> h.toResponse(tagsByHabitId.getOrDefault(h.getHabitId(), Set.of())))
                .toList();
    }

    @Override
    public GetHabitResponse updateHabit(UpdateHabitRequest updateHabitRequest, Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
        habit.update(updateHabitRequest);
        updateTags(habitId, updateHabitRequest.getTags());
        Set<String> tags = habitTagRepository.findTagNamesByHabitId(habitId);
        return habit.toResponse(tags);
    }

    @Override
    public void deleteHabit(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
        habitTagRepository.deleteByHabit(habit);
        habitRepository.delete(habit);
    }

    @Override
    public void updateTags(Long habitId, List<String> requestedTagNames) {
        if (requestedTagNames == null) return;

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        List<Tag> targetTags = tagService.getOrCreateEntities(requestedTagNames);
        Set<String> newTags = targetTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<String> oldTags = habitTagRepository.findTagNamesByHabitId(habitId);

        Set<String> toRemove = new HashSet<>(oldTags);
        toRemove.removeAll(newTags);

        Set<String> toAdd = new HashSet<>(newTags);
        toAdd.removeAll(oldTags);

        if (!toRemove.isEmpty()) {
            habitTagRepository.deleteByHabitIdAndTagNames(habitId, toRemove);
        }

        if (!toAdd.isEmpty()) {
            List<HabitTag> links = targetTags.stream()
                    .filter(tag -> toAdd.contains(tag.getName()))
                    .map(tag -> HabitTag.builder().habit(habit).tag(tag).build())
                    .toList();
            habitTagRepository.saveAll(links);
        }
    }
}
