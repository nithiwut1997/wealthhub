package com.nithiwut.wealthhub.transaction.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.entity.Portfolio;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import com.nithiwut.wealthhub.transaction.dto.request.CreateTransactionRequest;
import com.nithiwut.wealthhub.transaction.dto.response.TransactionResponse;
import com.nithiwut.wealthhub.transaction.entity.InvestmentTransaction;
import com.nithiwut.wealthhub.transaction.entity.TransactionType;
import com.nithiwut.wealthhub.transaction.repository.InvestmentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentTransactionService {
    private static final int CALCULATION_SCALE = 8;
    private static final RoundingMode CALCULATION_ROUNDING = RoundingMode.HALF_UP;

    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final HoldingRepository holdingRepository;
    private final InvestmentTransactionRepository transactionRepository;

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        validatePositive(request.quantity(), "Quantity");
        validatePositive(request.price(), "Price");

        Portfolio portfolio = portfolioRepository.findById(request.portfolioId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND, "Portfolio not found"));
        Asset asset = assetRepository.findById(request.assetId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.ASSET_NOT_FOUND, "Asset not found"));

        if (request.type() == null) {
            throw new BadRequestException("Transaction type must not be null");
        }

        if (request.type() == TransactionType.BUY) {
            applyBuy(portfolio, asset, request.quantity(), request.price());
        } else {
            applySell(portfolio, asset, request.quantity());
        }

        InvestmentTransaction transaction = InvestmentTransaction.builder()
            .portfolio(portfolio)
            .asset(asset)
            .type(request.type())
            .quantity(request.quantity())
            .price(request.price())
            .build();
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new NotFoundException(ErrorCode.PORTFOLIO_NOT_FOUND, "Portfolio not found");
        }
        return transactionRepository.findHistoryByPortfolioId(portfolioId).stream()
            .map(this::toResponse)
            .toList();
    }

    private void applyBuy(Portfolio portfolio, Asset asset, BigDecimal quantity, BigDecimal price) {
        Holding holding = holdingRepository.findByPortfolioIdAndAssetId(portfolio.getId(), asset.getId())
            .orElse(null);
        if (holding == null) {
            holdingRepository.save(Holding.builder()
                .portfolio(portfolio)
                .asset(asset)
                .quantity(quantity)
                .averageCost(price)
                .build());
            return;
        }

        BigDecimal newQuantity = holding.getQuantity().add(quantity);
        BigDecimal totalCost = holding.getQuantity().multiply(holding.getAverageCost())
            .add(quantity.multiply(price));
        BigDecimal newAverageCost = totalCost.divide(newQuantity, CALCULATION_SCALE, CALCULATION_ROUNDING);
        holding.updatePosition(newQuantity, newAverageCost);
    }

    private void applySell(Portfolio portfolio, Asset asset, BigDecimal quantity) {
        Holding holding = holdingRepository.findByPortfolioIdAndAssetId(portfolio.getId(), asset.getId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.HOLDING_NOT_FOUND,
                "Holding not found for the portfolio and asset"));
        if (quantity.compareTo(holding.getQuantity()) > 0) {
            throw new BadRequestException(ErrorCode.INSUFFICIENT_HOLDING_QUANTITY,
                "Sell quantity exceeds available holding quantity");
        }

        BigDecimal remainingQuantity = holding.getQuantity().subtract(quantity);
        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.updatePosition(remainingQuantity, holding.getAverageCost());
        }
    }

    private void validatePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(fieldName + " must be greater than zero");
        }
    }

    private TransactionResponse toResponse(InvestmentTransaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getPortfolio().getId(),
            transaction.getAsset().getId(),
            transaction.getAsset().getSymbol(),
            transaction.getType(),
            transaction.getQuantity(),
            transaction.getPrice(),
            transaction.getCreatedAt()
        );
    }
}
