package com.example.tracklybe.domain.tag.service;

import com.example.tracklybe.domain.tag.dto.TagResponse;
import com.example.tracklybe.domain.tag.entity.Tag;
import com.example.tracklybe.domain.tag.repository.TagRepository;
import com.example.tracklybe.global.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void getOrCreateEntities_returnsEmptyList_whenInputIsNull() {
        List<Tag> result = tagService.getOrCreateEntities(null);

        assertThat(result).isEmpty();
        verify(tagRepository, never()).findByNormalizedNameIn(anyList());
        verify(tagRepository, never()).saveAll(anyList());
    }

    @Test
    void getOrCreateEntities_returnsEmptyList_whenAllNamesAreBlankOrNull() {
        List<Tag> result = tagService.getOrCreateEntities(List.of("   ", "\t", ""));

        assertThat(result).isEmpty();
        verify(tagRepository, never()).findByNormalizedNameIn(anyList());
        verify(tagRepository, never()).saveAll(anyList());
    }

    @Test
    void getOrCreateEntities_createsMissingTags_andPreservesRequestedOrder() {
        Tag existing = Tag.builder()
                .tagId(1L)
                .name("Morning Run")
                .normalizedName("morning-run")
                .build();

        Tag created = Tag.builder()
                .tagId(2L)
                .name("Read Book")
                .normalizedName("read-book")
                .build();

        when(tagRepository.findByNormalizedNameIn(List.of("morning-run", "read-book")))
                .thenReturn(List.of(existing));
        when(tagRepository.saveAll(anyList())).thenReturn(List.of(created));

        List<Tag> result = tagService.getOrCreateEntities(List.of("  Morning Run  ", "Read   Book"));

        assertThat(result)
                .extracting(Tag::getNormalizedName)
                .containsExactly("morning-run", "read-book");

        assertThat(result)
                .extracting(Tag::getName)
                .containsExactly("Morning Run", "Read Book");
    }

    @Test
    void getOrCreateEntities_deduplicatesByNormalizedName_andKeepsFirstDisplayName() {
        when(tagRepository.findByNormalizedNameIn(List.of("morning-run"))).thenReturn(List.of());
        when(tagRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Tag> result = tagService.getOrCreateEntities(List.of(" Morning Run ", "morning   run"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNormalizedName()).isEqualTo("morning-run");
        assertThat(result.get(0).getName()).isEqualTo("Morning Run");

        ArgumentCaptor<List<Tag>> captor = ArgumentCaptor.forClass(List.class);
        verify(tagRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getName()).isEqualTo("Morning Run");
    }

    @Test
    void getOrCreateEntities_throwsInvalidRequestException_whenDisplayNameTooLong() {
        String tooLong = "a".repeat(31);
        List<String> tags = List.of(tooLong);

        assertThatThrownBy(() -> tagService.getOrCreateEntities(tags))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("태그명은 30자 이하여야 합니다.");

        verify(tagRepository, never()).findByNormalizedNameIn(anyList());
        verify(tagRepository, never()).saveAll(anyList());
    }

    @Test
    void getOrCreateAll_mapsTagsToTagResponses() {
        Tag existing = Tag.builder()
                .tagId(10L)
                .name("Workout")
                .normalizedName("workout")
                .build();

        when(tagRepository.findByNormalizedNameIn(List.of("workout"))).thenReturn(List.of(existing));

        List<TagResponse> result = tagService.getOrCreateAll(List.of("Workout"));

        assertThat(result).containsExactly(new TagResponse(10L, "Workout"));
        verify(tagRepository, never()).saveAll(anyList());
    }
}
