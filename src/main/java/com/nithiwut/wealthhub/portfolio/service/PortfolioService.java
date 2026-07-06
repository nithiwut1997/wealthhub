package com.nithiwut.wealthhub.portfolio.service;

import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.portfolio.dto.request.CreatePortfolioRequest;
import com.nithiwut.wealthhub.portfolio.dto.response.PortfolioResponse;
import com.nithiwut.wealthhub.portfolio.entity.Portfolio;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private static final String DEFAULT_BASE_CURRENCY = "THB";
    private final PortfolioRepository portfolioRepository;

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
    public PortfolioResponse getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND, "Portfolio not found"));
        return toResponse(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAllPortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        return portfolios.stream()
                .map(this::toResponse)
                .toList();
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
            portfolio.getId(),
            portfolio.getName(),
            portfolio.getBaseCurrency()
        );
    }
}
