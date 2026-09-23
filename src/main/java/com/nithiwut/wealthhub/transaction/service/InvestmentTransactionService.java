package com.nithiwut.wealthhub.transaction.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import com.nithiwut.wealthhub.common.exception.ConflictException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentTransactionService {
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
        int updatedRows = holdingRepository.applyBuy(portfolio.getId(), asset.getId(), quantity, price);
        if (updatedRows == 0) {
            try {
                holdingRepository.saveAndFlush(Holding.builder()
                    .portfolio(portfolio)
                    .asset(asset)
                    .quantity(quantity)
                    .averageCost(price)
                    .build());
            } catch (DataIntegrityViolationException exception) {
                throw new ConflictException(
                    ErrorCode.DUPLICATE_HOLDING,
                    "A holding was created concurrently; retry the transaction"
                );
            }
        }
    }

    private void applySell(Portfolio portfolio, Asset asset, BigDecimal quantity) {
        int updatedRows = holdingRepository.applySell(portfolio.getId(), asset.getId(), quantity);
        if (updatedRows == 0) {
            if (!holdingRepository.existsByPortfolioIdAndAssetId(portfolio.getId(), asset.getId())) {
                throw new NotFoundException(ErrorCode.HOLDING_NOT_FOUND,
                    "Holding not found for the portfolio and asset");
            }
            throw new BadRequestException(ErrorCode.INSUFFICIENT_HOLDING_QUANTITY,
                "Sell quantity exceeds available holding quantity");
        }
        holdingRepository.deleteClosedPosition(portfolio.getId(), asset.getId());
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
