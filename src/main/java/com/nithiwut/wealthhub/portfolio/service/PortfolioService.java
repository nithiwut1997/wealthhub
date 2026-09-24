package com.nithiwut.wealthhub.portfolio.service;

import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.dto.request.CreatePortfolioRequest;
import com.nithiwut.wealthhub.portfolio.dto.response.PortfolioResponse;
import com.nithiwut.wealthhub.portfolio.dto.response.PortfolioSummaryResponse;
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
public class PortfolioService {
    private static final String DEFAULT_BASE_CURRENCY = "THB";

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;

    @Transactional
    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        Portfolio portfolio = Portfolio.builder()
            .name(request.name())
            .baseCurrency(DEFAULT_BASE_CURRENCY)
            .build();
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return toResponse(savedPortfolio);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        return toResponse(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAllPortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        return portfolios.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary(Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);

        Map<Long, AssetPrice> latestPrices = holdings.isEmpty()
            ? Map.of()
            : assetPriceRepository.findLatestByAssetIds(
                    holdings.stream().map(holding -> holding.getAsset().getId()).toList())
                .stream()
                .collect(Collectors.toMap(price -> price.getAsset().getId(), Function.identity()));

        PortfolioValuation valuation = PortfolioValuationCalculator.calculate(
            holdings, latestPrices, portfolio.getBaseCurrency());

        return new PortfolioSummaryResponse(
            portfolio.getId(),
            portfolio.getName(),
            portfolio.getBaseCurrency(),
            valuation.holdingCount(),
            valuation.pricedHoldingCount(),
            valuation.missingPriceCount(),
            valuation.totalCost(),
            valuation.totalMarketValue(),
            valuation.unrealizedGainLoss(),
            valuation.unrealizedGainLossPercent(),
            portfolio.getUpdatedAt()
        );
    }

    private Portfolio getPortfolio(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new NotFoundException(
                ErrorCode.PORTFOLIO_NOT_FOUND, "Portfolio not found with id: " + portfolioId));
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
            portfolio.getId(),
            portfolio.getName(),
            portfolio.getBaseCurrency()
        );
    }
}
