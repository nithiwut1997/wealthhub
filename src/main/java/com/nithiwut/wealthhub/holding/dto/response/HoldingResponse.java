package com.nithiwut.wealthhub.holding.dto.response;

import java.math.BigDecimal;

public record HoldingResponse(
    Long id,
    Long assetId,
    String symbol,
    String name,
    BigDecimal quantity,
    BigDecimal averageCost
) {
}
