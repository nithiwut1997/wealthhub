package com.nithiwut.wealthhub.dashboard.service;

import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import com.nithiwut.wealthhub.dashboard.dto.response.PortfolioBreakdownResponse;
import com.nithiwut.wealthhub.dashboard.dto.response.DashboardResponse;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.entity.Portfolio;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import com.nithiwut.wealthhub.valuation.PortfolioValuation;
import com.nithiwut.wealthhub.valuation.PortfolioValuationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final String BASE_CURRENCY = "THB";

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final AssetRepository assetRepository;
    private final AssetPriceRepository assetPriceRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        List<Holding> holdings = holdingRepository.findAllWithPortfolioAndAsset();
        Map<Long, AssetPrice> latestPrices = holdings.isEmpty()
            ? Map.of()
            : assetPriceRepository.findLatestByAssetIds(holdings.stream()
                    .map(holding -> holding.getAsset().getId()).distinct().toList())
                .stream()
                .collect(Collectors.toMap(price -> price.getAsset().getId(), Function.identity()));
        Map<Long, List<Holding>> holdingsByPortfolio = holdings.stream()
            .collect(Collectors.groupingBy(holding -> holding.getPortfolio().getId()));

        List<PortfolioValuation> valuations = portfolios.stream()
            .map(portfolio -> valuePortfolio(portfolio, holdingsByPortfolio, latestPrices))
            .toList();
        PortfolioValuation total = PortfolioValuationCalculator.combine(valuations);
        List<PortfolioBreakdownResponse> breakdown = java.util.stream.IntStream
            .range(0, portfolios.size())
            .mapToObj(index -> toBreakdown(portfolios.get(index), valuations.get(index)))
            .toList();

        return new DashboardResponse(
            BASE_CURRENCY,
            (long) portfolios.size(),
            (long) total.holdingCount(),
            assetRepository.count(),
            total.totalCost(),
            total.totalMarketValue(),
            total.unrealizedGainLoss(),
            total.unrealizedGainLossPercent(),
            (long) total.pricedHoldingCount(),
            (long) total.missingPriceCount(),
            breakdown
        );
    }

    private PortfolioValuation valuePortfolio(
        Portfolio portfolio,
        Map<Long, List<Holding>> holdingsByPortfolio,
        Map<Long, AssetPrice> latestPrices
    ) {
        if (!BASE_CURRENCY.equalsIgnoreCase(portfolio.getBaseCurrency())) {
            throw new BadRequestException(ErrorCode.UNSUPPORTED_CURRENCY,
                "Cannot include portfolio " + portfolio.getName() + " in THB dashboard because its base currency is "
                    + portfolio.getBaseCurrency() + "; FX conversion is not supported");
        }
        return PortfolioValuationCalculator.calculate(
            holdingsByPortfolio.getOrDefault(portfolio.getId(), List.of()),
            latestPrices,
            portfolio.getBaseCurrency());
    }

    private PortfolioBreakdownResponse toBreakdown(Portfolio portfolio, PortfolioValuation valuation) {
        return new PortfolioBreakdownResponse(
            portfolio.getId(),
            portfolio.getName(),
            valuation.totalCost(),
            valuation.totalMarketValue(),
            valuation.unrealizedGainLoss(),
            valuation.unrealizedGainLossPercent(),
            valuation.holdingCount(),
            valuation.pricedHoldingCount(),
            valuation.missingPriceCount()
        );
    }
}
