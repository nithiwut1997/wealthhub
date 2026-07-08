package com.nithiwut.wealthhub.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PortfolioSummaryResponse(
    Long portfolioId,
    String portfolioName,
    String baseCurrency,
    Integer holdingCount,
    BigDecimal totalCost,
    BigDecimal totalMarketValue,
    BigDecimal unrealizedGainLoss,
    BigDecimal unrealizedGainLossPercent,
    LocalDateTime updatedAt
) {
}
