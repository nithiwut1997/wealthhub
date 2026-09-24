package com.nithiwut.wealthhub.valuation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ValuationCalculator {
    public static final int MONEY_SCALE = 8;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private ValuationCalculator() {
    }

    public static HoldingValuation calculate(
        BigDecimal quantity,
        BigDecimal averageCost,
        BigDecimal latestPrice
    ) {
        BigDecimal costBasis = costBasis(quantity, averageCost);
        BigDecimal marketValue = marketValue(quantity, latestPrice);
        return new HoldingValuation(costBasis, marketValue, marketValue.subtract(costBasis));
    }

    public static BigDecimal costBasis(BigDecimal quantity, BigDecimal averageCost) {
        return quantity.multiply(averageCost).setScale(MONEY_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal marketValue(BigDecimal quantity, BigDecimal latestPrice) {
        return quantity.multiply(latestPrice).setScale(MONEY_SCALE, ROUNDING_MODE);
    }
}
