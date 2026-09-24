package com.nithiwut.wealthhub.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    String baseCurrency,
    Long portfolioCount,
    Long holdingCount,
    Long assetCount,
    BigDecimal totalCost,
    BigDecimal totalMarketValue,
    BigDecimal unrealizedGainLoss,
    BigDecimal unrealizedGainLossPercent,
    Long pricedHoldingCount,
    Long missingPriceCount,
    List<PortfolioBreakdownResponse> portfolios
) {
}
