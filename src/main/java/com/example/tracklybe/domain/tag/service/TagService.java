package com.example.tracklybe.domain.tag.service;

import com.example.tracklybe.domain.tag.dto.TagResponse;

import java.util.Collection;
import java.util.List;

public interface TagService {

    TagResponse saveTag(String name);

    List<TagResponse> getOrCreateAll(Collection<String> rawNames);
}
