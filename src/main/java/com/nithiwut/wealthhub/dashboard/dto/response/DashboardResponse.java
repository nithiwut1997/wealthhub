package com.nithiwut.wealthhub.dashboard.dto.response;

import java.math.BigDecimal;

public record DashboardResponse(
    Long portfolioCount,
    Long holdingCount,
    Long assetCount,
    BigDecimal totalCost
) {
}
