package com.nithiwut.wealthhub.transaction.dto.request;

import com.nithiwut.wealthhub.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransactionRequest(
    @NotNull Long portfolioId,
    @NotNull Long assetId,
    @NotNull TransactionType type,
    @NotNull @Positive BigDecimal quantity,
    @NotNull @Positive BigDecimal price
) {
}
