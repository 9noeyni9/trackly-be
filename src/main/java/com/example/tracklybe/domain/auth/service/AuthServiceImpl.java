package com.example.tracklybe.domain.auth.service;

import com.example.tracklybe.domain.auth.dto.LoginRequest;
import com.example.tracklybe.domain.auth.dto.LoginResponse;
import com.example.tracklybe.domain.auth.dto.RefreshTokenRequest;
import com.example.tracklybe.domain.auth.entity.RefreshToken;
import com.example.tracklybe.domain.auth.repository.RefreshTokenStore;
import com.example.tracklybe.domain.user.entity.User;
import com.example.tracklybe.domain.user.repository.UserRepository;
import com.example.tracklybe.global.exception.UnauthorizedException;
import com.example.tracklybe.global.security.JwtTokenProvider;
import com.example.tracklybe.global.security.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return issueTokens(user);
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest refreshTokenRequest) {
        String rawRefreshToken = refreshTokenRequest.getRefreshToken();
        if (!jwtTokenProvider.validateRefreshToken(rawRefreshToken)) {
            throw new UnauthorizedException("유효하지 않은 refresh token입니다.");
        }

        String refreshTokenHash = refreshTokenHasher.hash(rawRefreshToken);
        RefreshToken savedToken = refreshTokenStore.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new UnauthorizedException("유효하지 않은 refresh token입니다."));

        if (savedToken.isExpired(LocalDateTime.now())) {
            throw new UnauthorizedException("만료된 refresh token입니다.");
        }

        Long userIdFromToken = jwtTokenProvider.getUserId(rawRefreshToken);
        if (!savedToken.getUser().getUserId().equals(userIdFromToken)) {
            throw new UnauthorizedException("유효하지 않은 refresh token입니다.");
        }

        return issueTokens(savedToken.getUser());
    }

    private LoginResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

        LocalDateTime refreshExpiresAt = jwtTokenProvider.getRefreshTokenExpiresAt();

        RefreshToken tokenEntity = refreshTokenStore.findByUserId(user.getUserId())
                .orElseGet(() -> RefreshToken.builder()
                        .user(user)
                        .tokenHash(refreshTokenHash)
                        .expiresAt(refreshExpiresAt)
                        .build());

        if (tokenEntity.getRefreshTokenId() != null) {
            tokenEntity.rotate(refreshTokenHash, refreshExpiresAt);
        }
        try {
            refreshTokenStore.save(tokenEntity);
        } catch (OptimisticLockingFailureException e) {
            throw new UnauthorizedException("동시에 갱신되어 refresh token이 무효화되었습니다. 다시 시도해주세요.");
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .refreshTokenExpiresIn(jwtTokenProvider.getRefreshTokenValiditySeconds())
                .build();
    }
}
