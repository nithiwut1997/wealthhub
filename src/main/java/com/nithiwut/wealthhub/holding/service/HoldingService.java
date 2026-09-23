package com.nithiwut.wealthhub.holding.service;

import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.holding.dto.response.HoldingResponse;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingService {
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;

    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldingsByPortfolioId(Long portfolioId) {
        portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new NotFoundException(
                    ErrorCode.PORTFOLIO_NOT_FOUND, "Portfolio not found with id: " + portfolioId));

        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        return holdings.stream().map(this::toResponse).toList();
    }

    private HoldingResponse toResponse(Holding holding) {
        return new HoldingResponse(
            holding.getId(),
            holding.getAsset().getId(),
            holding.getAsset().getSymbol(),
            holding.getAsset().getName(),
            holding.getQuantity(),
            holding.getAverageCost()
        );
    }
}
