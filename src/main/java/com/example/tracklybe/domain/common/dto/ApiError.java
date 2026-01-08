package com.example.tracklybe.domain.common.dto;

import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        String detail
) {
    public static ApiError of(ErrorCode errorCode, String detail) {
        return new ApiError(errorCode.name(), errorCode.getMessage(), detail);
    }
}
