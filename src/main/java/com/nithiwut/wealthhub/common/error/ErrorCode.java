package com.nithiwut.wealthhub.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND),

    HOLDING_NOT_FOUND(HttpStatus.NOT_FOUND),
    DUPLICATE_HOLDING(HttpStatus.CONFLICT),
    INSUFFICIENT_HOLDING_QUANTITY(HttpStatus.BAD_REQUEST),

    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND),
    ASSET_PRICE_NOT_FOUND(HttpStatus.NOT_FOUND),
    DUPLICATE_ASSET(HttpStatus.CONFLICT),

    UNSUPPORTED_CURRENCY(HttpStatus.BAD_REQUEST),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }
}
