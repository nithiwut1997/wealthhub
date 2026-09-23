package com.nithiwut.wealthhub.common.exception;

import com.nithiwut.wealthhub.common.error.ErrorCode;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
