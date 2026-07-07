package com.nithiwut.wealthhub.common.exception;

import com.nithiwut.wealthhub.common.error.ErrorCode;

public class ConflictException extends ApiException {
    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
