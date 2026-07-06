package com.nithiwut.wealthhub.common.exception;

import com.nithiwut.wealthhub.common.error.ErrorCode;

public class NotFoundException extends ApiException {
    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
