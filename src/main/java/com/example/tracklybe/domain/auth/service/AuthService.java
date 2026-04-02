package com.example.tracklybe.domain.auth.service;

import com.example.tracklybe.domain.auth.dto.LoginRequest;
import com.example.tracklybe.domain.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
}
