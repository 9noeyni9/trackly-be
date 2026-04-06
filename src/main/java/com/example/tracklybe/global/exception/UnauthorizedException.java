package com.example.tracklybe.global.exception;

import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final ErrorCode errorCode;

    public UnauthorizedException(String detailMessage) {
        super(detailMessage);
        this.errorCode = ErrorCode.UNAUTHORIZED;
    }
}
