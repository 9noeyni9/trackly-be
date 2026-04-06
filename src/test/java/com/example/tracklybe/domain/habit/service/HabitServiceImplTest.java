package com.example.tracklybe.domain.habit.service;

import com.example.tracklybe.domain.habit.dto.request.CreateHabitRequest;
import com.example.tracklybe.domain.habit.dto.request.UpdateHabitRequest;
import com.example.tracklybe.domain.habit.dto.response.CreateHabitResponse;
import com.example.tracklybe.domain.habit.dto.response.GetHabitResponse;
import com.example.tracklybe.domain.habit.entity.Habit;
import com.example.tracklybe.domain.habit.entity.HabitFrequency;
import com.example.tracklybe.domain.habit.entity.HabitTag;
import com.example.tracklybe.domain.habit.repository.HabitRepository;
import com.example.tracklybe.domain.habit.repository.HabitTagRepository;
import com.example.tracklybe.domain.tag.entity.Tag;
import com.example.tracklybe.domain.tag.service.TagService;
import com.example.tracklybe.domain.user.entity.User;
import com.example.tracklybe.domain.user.repository.UserRepository;
import com.example.tracklybe.global.exception.HabitNotFoundException;
import com.example.tracklybe.global.exception.InvalidRequestException;
import com.example.tracklybe.global.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitServiceImplTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitTagRepository habitTagRepository;

    @Mock
    private TagService tagService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private HabitServiceImpl habitService;

    @Test
    void createHabit_savesHabitAndTagLinks_whenTagsExist() {
        CreateHabitRequest request = mockCreateRequest(
                "Morning Run",
                "30 minutes",
                HabitFrequency.DAILY,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 12, 31),
                List.of("Health", "Routine")
        );

        Habit savedHabit = habit(1L, "Morning Run", "30 minutes", HabitFrequency.DAILY,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31));
        Tag health = tag(10L, "Health", "health");
        Tag routine = tag(11L, "Routine", "routine");
        User owner = user(100L, "owner@test.com");

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(owner));
        when(habitRepository.save(any(Habit.class))).thenReturn(savedHabit);
        when(tagService.getOrCreateEntities(List.of("Health", "Routine"))).thenReturn(List.of(health, routine));

        CreateHabitResponse response = habitService.createHabit(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Morning Run");
        assertThat(response.getTags()).containsExactly("Health", "Routine");

        ArgumentCaptor<List<HabitTag>> linksCaptor = ArgumentCaptor.forClass(List.class);
        verify(habitTagRepository).saveAll(linksCaptor.capture());
        List<HabitTag> links = linksCaptor.getValue();
        assertThat(links).hasSize(2);
        assertThat(links).extracting(link -> link.getHabit().getHabitId()).containsOnly(1L);
        assertThat(links).extracting(link -> link.getTag().getName()).containsExactlyInAnyOrder("Health", "Routine");
    }

    @Test
    void createHabit_doesNotSaveTagLinks_whenNoTagsReturned() {
        CreateHabitRequest request = mockCreateRequest(
                "Read",
                null,
                HabitFrequency.DAILY,
                null,
                null,
                List.of("Read")
        );

        Habit savedHabit = habit(2L, "Read", null, HabitFrequency.DAILY, null, null);
        User owner = user(100L, "owner@test.com");

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(owner));
        when(habitRepository.save(any(Habit.class))).thenReturn(savedHabit);
        when(tagService.getOrCreateEntities(List.of("Read"))).thenReturn(List.of());

        CreateHabitResponse response = habitService.createHabit(request);

        assertThat(response.getTags()).isEmpty();
        verify(habitTagRepository, never()).saveAll(anyList());
    }

    @Test
    void createHabit_throwsInvalidRequestException_whenStartDateAfterEndDate() {
        CreateHabitRequest request = mockCreateRequest(
                "Invalid",
                null,
                HabitFrequency.DAILY,
                LocalDate.of(2026, 4, 2),
                LocalDate.of(2026, 4, 1),
                List.of("Tag")
        );

        assertThatThrownBy(() -> habitService.createHabit(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("시작일은 종료일보다 늦을 수 없습니다.");

        verify(habitRepository, never()).save(any(Habit.class));
    }

    @Test
    void getHabit_returnsResponseWithTags() {
        Habit existing = habit(3L, "Stretch", "10 min", HabitFrequency.DAILY, null, null);
        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(3L, 100L)).thenReturn(Optional.of(existing));
        when(habitTagRepository.findTagNamesByHabitId(3L)).thenReturn(Set.of("Health"));

        GetHabitResponse response = habitService.getHabit(3L);

        assertThat(response.getHabitId()).isEqualTo(3L);
        assertThat(response.getTitle()).isEqualTo("Stretch");
        assertThat(response.getTags()).containsExactly("Health");
    }

    @Test
    void getHabit_throwsHabitNotFoundException_whenHabitMissing() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(99L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.getHabit(99L))
                .isInstanceOf(HabitNotFoundException.class)
                .hasMessageContaining("Habit with id: 99");
    }

    @Test
    void getAllHabits_returnsEmptyList_whenNoHabits() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findAllByOwnerUserId(100L)).thenReturn(List.of());

        List<GetHabitResponse> result = habitService.getAllHabits();

        assertThat(result).isEmpty();
        verify(habitTagRepository, never()).findHabitIdAndTagNameByHabitIds(anyList());
    }

    @Test
    void getAllHabits_aggregatesTagsByHabitId() {
        Habit first = habit(1L, "Run", null, HabitFrequency.DAILY, null, null);
        Habit second = habit(2L, "Read", null, HabitFrequency.DAILY, null, null);

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findAllByOwnerUserId(100L)).thenReturn(List.of(first, second));
        when(habitTagRepository.findHabitIdAndTagNameByHabitIds(List.of(1L, 2L)))
                .thenReturn(List.of(
                        new Object[]{1L, "Health"},
                        new Object[]{1L, "Morning"},
                        new Object[]{2L, "Learning"}
                ));

        List<GetHabitResponse> result = habitService.getAllHabits();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getHabitId()).isEqualTo(1L);
        assertThat(result.get(0).getTags()).containsExactlyInAnyOrder("Health", "Morning");
        assertThat(result.get(1).getHabitId()).isEqualTo(2L);
        assertThat(result.get(1).getTags()).containsExactly("Learning");
    }

    @Test
    void updateHabit_updatesFieldsAndReturnsLatestTags_whenRequestedTagsNull() {
        Habit existing = habit(5L, "Old", "desc", HabitFrequency.WEEKLY, null, null);
        UpdateHabitRequest request = mockUpdateRequest(
                "New",
                "new desc",
                HabitFrequency.MONTHLY,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 6, 30),
                null
        );

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(5L, 100L)).thenReturn(Optional.of(existing));
        when(habitTagRepository.findTagNamesByHabitId(5L)).thenReturn(Set.of("UpdatedTag"));

        GetHabitResponse result = habitService.updateHabit(request, 5L);

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("new desc");
        assertThat(result.getHabitFrequency()).isEqualTo(HabitFrequency.MONTHLY);
        assertThat(result.getTags()).containsExactly("UpdatedTag");

        verify(tagService, never()).getOrCreateEntities(anyCollection());
    }

    @Test
    void updateHabit_throwsInvalidRequestException_whenStartDateAfterEndDate() {
        Habit existing = habit(6L, "Old", "desc", HabitFrequency.WEEKLY, null, null);
        UpdateHabitRequest request = mockUpdateRequest(
                "New",
                "new desc",
                HabitFrequency.MONTHLY,
                LocalDate.of(2026, 5, 2),
                LocalDate.of(2026, 5, 1),
                List.of("Focus")
        );

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(6L, 100L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> habitService.updateHabit(request, 6L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("시작일은 종료일보다 늦을 수 없습니다.");

        verify(tagService, never()).getOrCreateEntities(anyCollection());
    }

    @Test
    void deleteHabit_deletesTagLinksThenHabit() {
        Habit existing = habit(7L, "Meditate", null, HabitFrequency.DAILY, null, null);
        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(7L, 100L)).thenReturn(Optional.of(existing));

        habitService.deleteHabit(7L);

        InOrder inOrder = org.mockito.Mockito.inOrder(habitTagRepository, habitRepository);
        inOrder.verify(habitTagRepository).deleteByHabit(existing);
        inOrder.verify(habitRepository).delete(existing);
    }

    @Test
    void updateTags_returnsImmediately_whenRequestedTagNamesIsNull() {
        habitService.updateTags(8L, null);

        verify(currentUserProvider, never()).getCurrentUserId();
        verify(habitRepository, never()).findByHabitIdAndOwnerUserId(anyLong(), anyLong());
        verify(tagService, never()).getOrCreateEntities(anyCollection());
        verify(habitTagRepository, never()).findTagNamesByHabitId(anyLong());
    }

    @Test
    void updateTags_removesMissingAndAddsNewTags() {
        Habit existing = habit(8L, "Study", null, HabitFrequency.DAILY, null, null);
        Tag health = tag(20L, "Health", "health");
        Tag focus = tag(21L, "Focus", "focus");

        when(currentUserProvider.getCurrentUserId()).thenReturn(100L);
        when(habitRepository.findByHabitIdAndOwnerUserId(8L, 100L)).thenReturn(Optional.of(existing));
        when(tagService.getOrCreateEntities(List.of("Health", "Focus"))).thenReturn(List.of(health, focus));
        when(habitTagRepository.findTagNamesByHabitId(8L)).thenReturn(Set.of("Health", "OldTag"));

        habitService.updateTags(8L, List.of("Health", "Focus"));

        ArgumentCaptor<Set<String>> removeCaptor = ArgumentCaptor.forClass(Set.class);
        verify(habitTagRepository).deleteByHabitIdAndTagNames(anyLong(), removeCaptor.capture());
        assertThat(removeCaptor.getValue()).containsExactly("OldTag");

        ArgumentCaptor<List<HabitTag>> addCaptor = ArgumentCaptor.forClass(List.class);
        verify(habitTagRepository).saveAll(addCaptor.capture());
        assertThat(addCaptor.getValue()).hasSize(1);
        assertThat(addCaptor.getValue().get(0).getTag().getName()).isEqualTo("Focus");
        assertThat(addCaptor.getValue().get(0).getHabit().getHabitId()).isEqualTo(8L);
    }

    private CreateHabitRequest mockCreateRequest(
            String title,
            String description,
            HabitFrequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            List<String> tags
    ) {
        CreateHabitRequest request = org.mockito.Mockito.mock(CreateHabitRequest.class);
        when(request.getTitle()).thenReturn(title);
        when(request.getDescription()).thenReturn(description);
        when(request.getHabitFrequency()).thenReturn(frequency);
        when(request.getStartDate()).thenReturn(startDate);
        when(request.getEndDate()).thenReturn(endDate);
        when(request.getTags()).thenReturn(tags);
        return request;
    }

    private UpdateHabitRequest mockUpdateRequest(
            String title,
            String description,
            HabitFrequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            List<String> tags
    ) {
        UpdateHabitRequest request = org.mockito.Mockito.mock(UpdateHabitRequest.class);
        when(request.getTitle()).thenReturn(title);
        when(request.getDescription()).thenReturn(description);
        when(request.getHabitFrequency()).thenReturn(frequency);
        when(request.getStartDate()).thenReturn(startDate);
        when(request.getEndDate()).thenReturn(endDate);
        when(request.getTags()).thenReturn(tags);
        return request;
    }

    private Habit habit(Long id, String title, String description, HabitFrequency frequency, LocalDate startDate, LocalDate endDate) {
        return Habit.builder()
                .habitId(id)
                .title(title)
                .description(description)
                .habitFrequency(frequency)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private Tag tag(Long id, String name, String normalizedName) {
        return Tag.builder()
                .tagId(id)
                .name(name)
                .normalizedName(normalizedName)
                .build();
    }

    private User user(Long userId, String email) {
        return User.builder()
                .userId(userId)
                .email(email)
                .password("encoded")
                .nickname("owner")
                .build();
    }
}
