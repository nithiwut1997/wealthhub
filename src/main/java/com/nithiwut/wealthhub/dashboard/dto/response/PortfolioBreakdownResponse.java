package com.nithiwut.wealthhub.dashboard.dto.response;

import java.math.BigDecimal;

public record PortfolioBreakdownResponse(
    Long portfolioId,
    String portfolioName,
    BigDecimal totalCost,
    BigDecimal totalMarketValue,
    BigDecimal unrealizedGainLoss,
    BigDecimal unrealizedGainLossPercent,
    Integer holdingCount,
    Integer pricedHoldingCount,
    Integer missingPriceCount
) {
}
