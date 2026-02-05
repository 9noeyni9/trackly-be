package com.example.tracklybe.domain.tag.entity;

import com.example.tracklybe.domain.common.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "tags"
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Tag extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @Column(nullable = false, length = 30)
    private String name;
}
