package com.nithiwut.wealthhub.portfolio.service;

import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.dto.request.CreatePortfolioRequest;
import com.nithiwut.wealthhub.portfolio.dto.response.PortfolioResponse;
import com.nithiwut.wealthhub.portfolio.dto.response.PortfolioSummaryResponse;
import com.nithiwut.wealthhub.portfolio.entity.Portfolio;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private static final String DEFAULT_BASE_CURRENCY = "THB";

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;

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

        BigDecimal totalCost = holdings.stream()
            .map(holding -> holding.getQuantity().multiply(holding.getAverageCost()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMarketValue = totalCost.multiply(BigDecimal.ONE);
        BigDecimal unrealizedGailLoss = totalMarketValue.subtract(totalCost);
        BigDecimal unrealizedGainLossPercent = totalCost.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : unrealizedGailLoss.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return new PortfolioSummaryResponse(
            portfolio.getId(),
            portfolio.getName(),
            portfolio.getBaseCurrency(),
            holdings.size(),
            totalCost,
            totalMarketValue,
            unrealizedGailLoss,
            unrealizedGainLossPercent,
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
