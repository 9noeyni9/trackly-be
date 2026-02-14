package com.example.tracklybe.domain.tag.service;

import com.example.tracklybe.domain.tag.dto.TagResponse;
import com.example.tracklybe.domain.tag.entity.Tag;

import java.util.Collection;
import java.util.List;

public interface TagService {

    List<Tag> getOrCreateEntities(Collection<String> rawNames);

    List<TagResponse> getOrCreateAll(Collection<String> rawNames);
}
