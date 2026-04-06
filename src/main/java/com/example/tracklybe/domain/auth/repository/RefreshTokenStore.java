package com.example.tracklybe.domain.auth.repository;

import com.example.tracklybe.domain.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenStore {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    Optional<RefreshToken> findByUserId(Long userId);
    RefreshToken save(RefreshToken refreshToken);
}
