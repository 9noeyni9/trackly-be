package com.example.tracklybe.domain.auth.service;

import com.example.tracklybe.domain.auth.dto.LoginRequest;
import com.example.tracklybe.domain.auth.dto.LoginResponse;
import com.example.tracklybe.domain.auth.dto.RefreshTokenRequest;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    LoginResponse refresh(RefreshTokenRequest refreshTokenRequest);
}
