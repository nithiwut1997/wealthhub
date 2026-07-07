package com.nithiwut.wealthhub.holding.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateHoldingRequest(
    Long portfolioId,
    Long assetId,

    @NotNull
    @Positive
    BigDecimal quantity,

    @PositiveOrZero
    BigDecimal averageCost
) {
}
