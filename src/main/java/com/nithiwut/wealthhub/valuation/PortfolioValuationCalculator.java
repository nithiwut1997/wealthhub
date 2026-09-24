package com.nithiwut.wealthhub.valuation;

import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import com.nithiwut.wealthhub.holding.entity.Holding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public final class PortfolioValuationCalculator {
    private PortfolioValuationCalculator() {
    }

    public static PortfolioValuation calculate(
        List<Holding> holdings,
        Map<Long, AssetPrice> latestPrices,
        String baseCurrency
    ) {
        BigDecimal totalCost = zeroMoney();
        BigDecimal pricedCost = zeroMoney();
        BigDecimal totalMarketValue = zeroMoney();
        int pricedHoldingCount = 0;

        for (Holding holding : holdings) {
            ensureMatchingCurrency(holding, baseCurrency);
            BigDecimal costBasis = ValuationCalculator.costBasis(
                holding.getQuantity(), holding.getAverageCost());
            totalCost = totalCost.add(costBasis);

            AssetPrice latestPrice = latestPrices.get(holding.getAsset().getId());
            if (latestPrice != null) {
                HoldingValuation valuation = ValuationCalculator.calculate(
                    holding.getQuantity(), holding.getAverageCost(), latestPrice.getPrice());
                pricedCost = pricedCost.add(valuation.costBasis());
                totalMarketValue = totalMarketValue.add(valuation.marketValue());
                pricedHoldingCount++;
            }
        }

        BigDecimal unrealizedGainLoss = totalMarketValue.subtract(pricedCost);
        BigDecimal unrealizedGainLossPercent = percentage(unrealizedGainLoss, pricedCost);

        return new PortfolioValuation(
            totalCost,
            pricedCost,
            totalMarketValue,
            unrealizedGainLoss,
            unrealizedGainLossPercent,
            holdings.size(),
            pricedHoldingCount,
            holdings.size() - pricedHoldingCount
        );
    }

    public static PortfolioValuation combine(List<PortfolioValuation> valuations) {
        BigDecimal totalCost = valuations.stream()
            .map(PortfolioValuation::totalCost).reduce(zeroMoney(), BigDecimal::add);
        BigDecimal pricedCost = valuations.stream()
            .map(PortfolioValuation::pricedCost).reduce(zeroMoney(), BigDecimal::add);
        BigDecimal totalMarketValue = valuations.stream()
            .map(PortfolioValuation::totalMarketValue).reduce(zeroMoney(), BigDecimal::add);
        BigDecimal unrealizedGainLoss = valuations.stream()
            .map(PortfolioValuation::unrealizedGainLoss).reduce(zeroMoney(), BigDecimal::add);
        int holdingCount = valuations.stream().mapToInt(PortfolioValuation::holdingCount).sum();
        int pricedHoldingCount = valuations.stream()
            .mapToInt(PortfolioValuation::pricedHoldingCount).sum();

        return new PortfolioValuation(
            totalCost,
            pricedCost,
            totalMarketValue,
            unrealizedGainLoss,
            percentage(unrealizedGainLoss, pricedCost),
            holdingCount,
            pricedHoldingCount,
            holdingCount - pricedHoldingCount
        );
    }

    private static BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(ValuationCalculator.MONEY_SCALE);
    }

    private static BigDecimal percentage(BigDecimal gainLoss, BigDecimal cost) {
        return cost.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : gainLoss.divide(cost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private static void ensureMatchingCurrency(Holding holding, String baseCurrency) {
        if (!baseCurrency.equalsIgnoreCase(holding.getAsset().getCurrency())) {
            throw new BadRequestException(ErrorCode.UNSUPPORTED_CURRENCY,
                "Cannot value asset " + holding.getAsset().getSymbol() + " in "
                    + holding.getAsset().getCurrency() + " for portfolio base currency " + baseCurrency
                    + "; FX conversion is not supported");
        }
    }
}
