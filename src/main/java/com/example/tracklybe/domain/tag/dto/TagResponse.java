package com.example.tracklybe.domain.tag.dto;

import com.example.tracklybe.domain.tag.entity.Tag;

public record TagResponse(Long tagId, String name) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getTagId(), tag.getName());
    }
}
