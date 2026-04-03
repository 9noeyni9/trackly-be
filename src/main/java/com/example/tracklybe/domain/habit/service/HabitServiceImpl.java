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
import com.example.tracklybe.domain.user.entity.User;
import com.example.tracklybe.domain.user.repository.UserRepository;
import com.example.tracklybe.global.exception.ForbiddenException;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import com.example.tracklybe.global.exception.InvalidRequestException;
import com.example.tracklybe.global.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public CreateHabitResponse createHabit(CreateHabitRequest createHabitRequest) {
        validateDateRange(createHabitRequest.getStartDate(), createHabitRequest.getEndDate());
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new InvalidRequestException("유효하지 않은 사용자입니다."));

        Habit habit = Habit.builder()
                .owner(owner)
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
        Long currentUserId = currentUserProvider.getCurrentUserId();
        Habit habit = findOwnedHabitOrThrow(habitId, currentUserId);
        Set<String> tags = habitTagRepository.findTagNamesByHabitId(habitId);
        return habit.toResponse(tags);
    }

    @Override
    public List<GetHabitResponse> getAllHabits() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        List<Habit> habits = habitRepository.findAllByOwnerUserId(currentUserId);
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
        Long currentUserId = currentUserProvider.getCurrentUserId();
        Habit habit = findOwnedHabitOrThrow(habitId, currentUserId);
        validateDateRange(updateHabitRequest.getStartDate(), updateHabitRequest.getEndDate());
        habit.update(updateHabitRequest);
        updateTags(habitId, updateHabitRequest.getTags());
        Set<String> tags = habitTagRepository.findTagNamesByHabitId(habitId);
        return habit.toResponse(tags);
    }

    @Override
    public void deleteHabit(Long habitId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        Habit habit = findOwnedHabitOrThrow(habitId, currentUserId);
        habitTagRepository.deleteByHabit(habit);
        habitRepository.delete(habit);
    }

    @Override
    public void updateTags(Long habitId, List<String> requestedTagNames) {
        if (requestedTagNames == null) return;
        Long currentUserId = currentUserProvider.getCurrentUserId();

        Habit habit = findOwnedHabitOrThrow(habitId, currentUserId);

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

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidRequestException("시작일은 종료일보다 늦을 수 없습니다.");
        }
    }

    private Habit findOwnedHabitOrThrow(Long habitId, Long currentUserId) {
        return habitRepository.findByHabitIdAndOwnerUserId(habitId, currentUserId)
                .orElseGet(() -> {
                    if (habitRepository.existsById(habitId)) {
                        throw new ForbiddenException("해당 습관에 접근 권한이 없습니다.");
                    }
                    throw new HabitNotFoundException(habitId);
                });
    }
}
