package com.example.tracklybe.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {

    @NotBlank(message = "refresh token을 입력해주세요.")
    private String refreshToken;
}
