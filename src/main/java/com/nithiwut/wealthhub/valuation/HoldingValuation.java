package com.nithiwut.wealthhub.valuation;

import java.math.BigDecimal;

public record HoldingValuation(
    BigDecimal costBasis,
    BigDecimal marketValue,
    BigDecimal unrealizedPnL
) {
}
