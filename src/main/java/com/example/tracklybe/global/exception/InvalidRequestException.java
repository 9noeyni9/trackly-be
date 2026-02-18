package com.example.tracklybe.global.exception;

import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidRequestException(String detailMessage) {
        super(detailMessage);
        this.errorCode = ErrorCode.INVALID_REQUEST;
    }
}
