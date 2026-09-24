package com.nithiwut.wealthhub.valuation;

import java.math.BigDecimal;

public record PortfolioValuation(
    BigDecimal totalCost,
    BigDecimal pricedCost,
    BigDecimal totalMarketValue,
    BigDecimal unrealizedGainLoss,
    BigDecimal unrealizedGainLossPercent,
    int holdingCount,
    int pricedHoldingCount,
    int missingPriceCount
) {
}
