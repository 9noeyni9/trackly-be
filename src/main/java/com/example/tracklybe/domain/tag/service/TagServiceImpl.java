package com.example.tracklybe.domain.tag.service;

import com.example.tracklybe.domain.tag.dto.TagResponse;
import com.example.tracklybe.domain.tag.entity.Tag;
import com.example.tracklybe.domain.tag.repository.TagRepository;
import com.example.tracklybe.global.exception.InvalidRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private static final int MAX_TAG_NAME_LENGTH = 30;
    private static final int MAX_NORMALIZED_NAME_LENGTH = 64;

    private final TagRepository tagRepository;

    @Override
    public List<Tag> getOrCreateEntities(Collection<String> rawNames) {
        LinkedHashMap<String, String> normalizedToDisplay = normalizeNames(rawNames);
        if (normalizedToDisplay.isEmpty()) return List.of();

        List<String> normalizedNames = new ArrayList<>(normalizedToDisplay.keySet());
        ExistingLookup lookup = findExistingByNormalizedNames(normalizedNames);
        List<Tag> allTags = createMissingTags(normalizedToDisplay, lookup);
        return orderByRequestedNormalizedNames(normalizedNames, allTags);
    }

    @Override
    public List<TagResponse> getOrCreateAll(Collection<String> rawNames) {
        return getOrCreateEntities(rawNames).stream()
                .map(TagResponse::from)
                .toList();
    }

    private LinkedHashMap<String, String> normalizeNames(Collection<String> rawNames) {
        LinkedHashMap<String, String> normalizedToDisplay = new LinkedHashMap<>();
        if (rawNames == null) return normalizedToDisplay;

        for (String rawName : rawNames) {
            if(rawName == null) continue;
            String displayName = normalizeDisplayName(rawName);
            if (displayName.isBlank()) continue;
            validateDisplayNameLength(displayName);

            String normalizedName = normalizeKey(displayName);
            validateNormalizedName(displayName, normalizedName);
            normalizedToDisplay.putIfAbsent(normalizedName, displayName);
        }
        return normalizedToDisplay;
    }

    private ExistingLookup findExistingByNormalizedNames(List<String> normalizedNames) {
        List<Tag> existing = new ArrayList<>(tagRepository.findByNormalizedNameIn(normalizedNames));
        List<String> existingNormalizedNames = existing.stream()
                .map(Tag::getNormalizedName)
                .toList();
        return new ExistingLookup(existing, existingNormalizedNames);
    }

    private List<Tag> createMissingTags(
            LinkedHashMap<String, String> normalizedToDisplay,
            ExistingLookup lookup
    ) {
        List<Tag> toCreate = normalizedToDisplay.entrySet().stream()
                .filter(entry -> !lookup.existingNormalizedNames().contains(entry.getKey()))
                .map(normalizedName -> Tag.builder()
                        .name(normalizedName.getValue())
                        .normalizedName(normalizedName.getKey())
                        .build())
                .toList();

        List<Tag> all = new ArrayList<>(lookup.existingTags());
        if (!toCreate.isEmpty()) {
            all.addAll(tagRepository.saveAll(toCreate));
        }
        return all;
    }

    private List<Tag> orderByRequestedNormalizedNames(List<String> normalizedNames, List<Tag> allTags) {
        Map<String, Tag> byNormalizedName = allTags.stream()
                .collect(Collectors.toMap(
                        Tag::getNormalizedName,
                        tag -> tag,
                        (existing, ignored) -> existing
                ));
        return normalizedNames.stream()
                .map(byNormalizedName::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private String normalizeDisplayName(String rawName) {
        String normalized = Normalizer.normalize(rawName, Normalizer.Form.NFKC).trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private String normalizeKey(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        String hyphenated = lower.replaceAll("\\s+", "-");
        String compactHyphen = hyphenated.replaceAll("-{2,}", "-");
        return trimEdgeHyphens(compactHyphen);
    }

    private String trimEdgeHyphens(String s) {
        int start = 0;
        int end = s.length();

        while (start < end && s.charAt(start) == '-') start++;
        while (end > start && s.charAt(end - 1) == '-') end--;

        return s.substring(start, end);
    }

    private void validateDisplayNameLength(String displayName) {
        if (displayName.length() > MAX_TAG_NAME_LENGTH) {
            throw new InvalidRequestException(
                    "태그명은 " + MAX_TAG_NAME_LENGTH + "자 이하여야 합니다. tag=" + displayName
            );
        }
    }

    private void validateNormalizedName(String displayName, String normalizedName) {
        if (normalizedName.isBlank()) {
            throw new InvalidRequestException("유효하지 않은 태그명입니다. tag=" + displayName);
        }
        if (normalizedName.length() > MAX_NORMALIZED_NAME_LENGTH) {
            throw new InvalidRequestException(
                    "정규화된 태그명은 " + MAX_NORMALIZED_NAME_LENGTH + "자 이하여야 합니다. tag=" + displayName
            );
        }
    }

    private record ExistingLookup(List<Tag> existingTags, List<String> existingNormalizedNames) {}
}
