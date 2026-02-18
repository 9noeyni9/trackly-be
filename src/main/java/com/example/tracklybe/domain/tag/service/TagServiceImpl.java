package com.example.tracklybe.domain.tag.service;

import com.example.tracklybe.domain.tag.dto.TagResponse;
import com.example.tracklybe.domain.tag.entity.Tag;
import com.example.tracklybe.domain.tag.repository.TagRepository;
import com.example.tracklybe.global.exception.InvalidRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator;

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
        ExistingLookup lookup = findExistingByNormalized(normalizedNames);
        backfillLegacyNormalizedNames(normalizedToDisplay, lookup);
        List<Tag> allTags = createMissingTags(normalizedToDisplay, normalizedNames, lookup);
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
            String displayName = normalizeDisplayName(rawName);
            if (rawName == null || displayName.isBlank()) continue;
            validateDisplayNameLength(displayName);

            String normalizedName = normalizeKey(displayName);
            validateNormalizedName(displayName, normalizedName);
            normalizedToDisplay.putIfAbsent(normalizedName, displayName);
        }
        return normalizedToDisplay;
    }

    private ExistingLookup findExistingByNormalized(List<String> normalizedNames) {
        List<Tag> existing = tagRepository.findByNormalizedNameIn(normalizedNames);
        Set<String> exists = existing.stream()
                .map(Tag::getNormalizedName)
                .collect(Collectors.toSet());
        return new ExistingLookup(existing, exists);
    }

    private void backfillLegacyNormalizedNames(
            LinkedHashMap<String, String> normalizedToDisplay,
            ExistingLookup lookup
    ) {
        List<Tag> legacyTags = findLegacyTagsByDisplayName(normalizedToDisplay.values());
        List<Tag> backfillTargets = selectBackfillTargets(legacyTags, lookup.existingNormalizedNames());

        for (Tag tag : legacyTags) {
            String normalizedName = normalizeKey(normalizeDisplayName(tag.getName()));
            if (!lookup.existingNormalizedNames().contains(normalizedName)) {
                lookup.existingTags().add(tag);
                lookup.existingNormalizedNames().add(normalizedName);
            }
        }

        saveBackfillTargets(backfillTargets);
    }

    private List<Tag> findLegacyTagsByDisplayName(Collection<String> displayNames) {
        return tagRepository.findByNameIn(new ArrayList<>(displayNames)).stream()
                .sorted(Comparator.comparing(Tag::getTagId))
                .toList();
    }

    private List<Tag> selectBackfillTargets(List<Tag> legacyTags, Set<String> occupiedNormalizedNames) {
        List<Tag> backfillTargets = new ArrayList<>();
        Set<String> occupied = new HashSet<>(occupiedNormalizedNames);
        Map<String, Tag> selectedByNormalizedName = new LinkedHashMap<>();

        for (Tag tag : legacyTags) {
            String normalizedName = normalizeKey(normalizeDisplayName(tag.getName()));
            if (tag.getNormalizedName() != null && !tag.getNormalizedName().isBlank() || occupied.contains(normalizedName) || selectedByNormalizedName.containsKey(normalizedName)) {
                continue;
            }
            tag.updateNormalizedName(normalizedName);
            selectedByNormalizedName.put(normalizedName, tag);
            occupied.add(normalizedName);
        }

        backfillTargets.addAll(selectedByNormalizedName.values());
        return backfillTargets;
    }

    private void saveBackfillTargets(List<Tag> backfillTargets) {
        if (backfillTargets.isEmpty()) return;
        try {
            tagRepository.saveAll(backfillTargets);
        } catch (DataIntegrityViolationException ignored) {
            // Legacy duplicate data can still race or conflict; request should continue with canonical lookup.
        }
    }

    private List<Tag> createMissingTags(
            LinkedHashMap<String, String> normalizedToDisplay,
            List<String> normalizedNames,
            ExistingLookup lookup
    ) {
        List<Tag> toCreate = normalizedNames.stream()
                .filter(n -> !lookup.existingNormalizedNames().contains(n))
                .map(normalizedName -> Tag.builder()
                        .name(normalizedToDisplay.get(normalizedName))
                        .normalizedName(normalizedName)
                        .build())
                .toList();

        List<Tag> all = new ArrayList<>(lookup.existingTags());
        if (toCreate.isEmpty()) {
            return all;
        }

        try {
            all.addAll(tagRepository.saveAll(toCreate));
            return all;
        } catch (DataIntegrityViolationException ignored) {
            // Concurrent creates may win for the same normalized key. Re-read canonical rows.
            return mergeByNormalizedName(all, tagRepository.findByNormalizedNameIn(normalizedNames));
        }
    }

    private List<Tag> orderByRequestedNormalizedNames(List<String> normalizedNames, List<Tag> allTags) {
        Map<String, Tag> byNormalizedName = allTags.stream()
                .collect(Collectors.toMap(Tag::getNormalizedName, tag -> tag));
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

    private List<Tag> mergeByNormalizedName(List<Tag> current, List<Tag> additional) {
        Map<String, Tag> merged = new LinkedHashMap<>();
        for (Tag tag : current) {
            if (tag.getNormalizedName() != null && !tag.getNormalizedName().isBlank()) {
                merged.putIfAbsent(tag.getNormalizedName(), tag);
            }
        }
        for (Tag tag : additional) {
            if (tag.getNormalizedName() != null && !tag.getNormalizedName().isBlank()) {
                merged.putIfAbsent(tag.getNormalizedName(), tag);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private record ExistingLookup(List<Tag> existingTags, Set<String> existingNormalizedNames) {
    }
}
