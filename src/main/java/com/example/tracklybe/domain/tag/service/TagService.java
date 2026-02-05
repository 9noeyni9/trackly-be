package com.example.tracklybe.domain.tag.service;

import com.example.tracklybe.domain.tag.entity.Tag;

import java.util.Collection;
import java.util.List;

public interface TagService {

    Tag saveTag(String name);

    List<Tag> getOrCreateAll(Collection<String> rawNames);
}
