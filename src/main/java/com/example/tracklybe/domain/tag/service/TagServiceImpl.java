package com.example.tracklybe.domain.tag.service;

import com.example.tracklybe.domain.tag.dto.TagResponse;
import com.example.tracklybe.domain.tag.entity.Tag;
import com.example.tracklybe.domain.tag.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public List<TagResponse> getOrCreateAll(Collection<String> rawNames) {
        if(rawNames == null) return List.of();

        List<String> names = rawNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if(names.isEmpty()) return List.of();

        List<Tag> existing = tagRepository.findByNameIn(names);
        Set<String> exists = existing.stream().map(Tag::getName).collect(Collectors.toSet());

        List<Tag> toCreate = names.stream()
                .filter(n -> !exists.contains(n))
                .map(name -> Tag.builder()
                        .name(name)
                        .build())
                .toList();

        if(!toCreate.isEmpty()) {
            existing = new ArrayList<>(existing);
            existing.addAll(tagRepository.saveAll(toCreate));
        }
        return existing.stream()
                .map(TagResponse::from)
                .toList();
    }

}
