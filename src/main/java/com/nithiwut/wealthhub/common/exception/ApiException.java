package com.nithiwut.wealthhub.common.exception;

import com.nithiwut.wealthhub.common.error.ErrorCode;
import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {
    private final ErrorCode errorCode;

    protected ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
