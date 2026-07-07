package com.nithiwut.wealthhub.holding.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.ConflictException;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.holding.dto.request.CreateHoldingRequest;
import com.nithiwut.wealthhub.holding.dto.response.HoldingResponse;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.entity.Portfolio;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingService {
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final HoldingRepository holdingRepository;

    @Transactional
    public HoldingResponse createHolding(CreateHoldingRequest request) {
        Portfolio portfolio = portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND, "Portfolio not found"));
        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASSET_NOT_FOUND, "Asset not found"));

        if (holdingRepository.existsByPortfolioIdAndAssetId(request.portfolioId(), request.assetId())) {
            throw new ConflictException(
                ErrorCode.DUPLICATE_HOLDING, "Duplicate holding for the same asset in the portfolio");
        }

        Holding holding = Holding.builder()
            .portfolio(portfolio)
            .asset(asset)
            .quantity(request.quantity())
            .averageCost(request.averageCost())
            .build();

        Holding savedHolding = holdingRepository.save(holding);
        return toResponse(savedHolding);
    }

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
