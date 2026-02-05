package com.example.tracklybe.domain.tag.repository;

import com.example.tracklybe.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String nameNormalized);
    List<Tag> findByNameIn(List<String> names);
}
