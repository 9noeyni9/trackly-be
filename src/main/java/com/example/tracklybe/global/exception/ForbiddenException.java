package com.example.tracklybe.global.exception;

import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    private final ErrorCode errorCode;

    public ForbiddenException(String detailMessage) {
        super(detailMessage);
        this.errorCode = ErrorCode.FORBIDDEN;
    }
}
