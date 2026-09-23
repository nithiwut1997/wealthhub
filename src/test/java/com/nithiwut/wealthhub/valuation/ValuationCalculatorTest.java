package com.nithiwut.wealthhub.valuation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationCalculatorTest {
    @Test
    void calculatesHoldingCostMarketValueAndUnrealizedProfitAtExplicitScale() {
        HoldingValuation valuation = ValuationCalculator.calculate(
            new BigDecimal("10"), new BigDecimal("110"), new BigDecimal("160"));

        assertThat(valuation.costBasis()).isEqualByComparingTo("1100.00000000");
        assertThat(valuation.marketValue()).isEqualByComparingTo("1600.00000000");
        assertThat(valuation.unrealizedPnL()).isEqualByComparingTo("500.00000000");
        assertThat(valuation.marketValue().scale()).isEqualTo(8);
    }
}
