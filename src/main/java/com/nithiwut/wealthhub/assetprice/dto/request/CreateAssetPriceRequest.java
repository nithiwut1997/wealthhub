package com.nithiwut.wealthhub.assetprice.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateAssetPriceRequest(
    @NotNull @Positive @Digits(integer = 12, fraction = 8) BigDecimal price,
    @NotNull LocalDateTime pricedAt
) {
}
