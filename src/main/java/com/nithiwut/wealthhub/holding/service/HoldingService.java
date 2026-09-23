package com.nithiwut.wealthhub.holding.service;

import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.holding.dto.response.HoldingResponse;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import com.nithiwut.wealthhub.valuation.HoldingValuation;
import com.nithiwut.wealthhub.valuation.ValuationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoldingService {
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;

    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldingsByPortfolioId(Long portfolioId) {
        var portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new NotFoundException(
                    ErrorCode.PORTFOLIO_NOT_FOUND, "Portfolio not found with id: " + portfolioId));

        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        if (holdings.isEmpty()) {
            return List.of();
        }
        Map<Long, AssetPrice> latestPrices = assetPriceRepository.findLatestByAssetIds(
                holdings.stream().map(holding -> holding.getAsset().getId()).toList())
            .stream()
            .collect(Collectors.toMap(price -> price.getAsset().getId(), Function.identity()));
        return holdings.stream().map(holding -> {
            ensureMatchingCurrency(holding, portfolio.getBaseCurrency());
            AssetPrice latestPrice = requireLatestPrice(holding, latestPrices);
            return toResponse(holding, latestPrice);
        }).toList();
    }

    private HoldingResponse toResponse(Holding holding, AssetPrice latestPrice) {
        HoldingValuation valuation = ValuationCalculator.calculate(
            holding.getQuantity(), holding.getAverageCost(), latestPrice.getPrice());
        return new HoldingResponse(
            holding.getId(),
            holding.getAsset().getId(),
            holding.getAsset().getSymbol(),
            holding.getAsset().getName(),
            holding.getQuantity(),
            holding.getAverageCost(),
            latestPrice.getPrice(),
            valuation.costBasis(),
            valuation.marketValue(),
            valuation.unrealizedPnL()
        );
    }

    private AssetPrice requireLatestPrice(Holding holding, Map<Long, AssetPrice> latestPrices) {
        AssetPrice price = latestPrices.get(holding.getAsset().getId());
        if (price == null) {
            throw new NotFoundException(ErrorCode.ASSET_PRICE_NOT_FOUND,
                "Cannot value holding because no market price exists for asset "
                    + holding.getAsset().getSymbol() + " (id: " + holding.getAsset().getId() + ")");
        }
        return price;
    }

    private void ensureMatchingCurrency(Holding holding, String baseCurrency) {
        if (!baseCurrency.equalsIgnoreCase(holding.getAsset().getCurrency())) {
            throw new BadRequestException(ErrorCode.UNSUPPORTED_CURRENCY,
                "Cannot value asset " + holding.getAsset().getSymbol() + " in "
                    + holding.getAsset().getCurrency() + " for portfolio base currency " + baseCurrency
                    + "; FX conversion is not supported");
        }
    }
}
