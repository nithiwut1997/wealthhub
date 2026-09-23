package com.nithiwut.wealthhub.transaction.dto.response;

import com.nithiwut.wealthhub.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    Long id,
    Long portfolioId,
    Long assetId,
    String assetSymbol,
    TransactionType type,
    BigDecimal quantity,
    BigDecimal price,
    BigDecimal realizedPnL,
    LocalDateTime createdAt
) {
}
