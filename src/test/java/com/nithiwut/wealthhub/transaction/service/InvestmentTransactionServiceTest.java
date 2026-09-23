package com.nithiwut.wealthhub.transaction.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.ApiException;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.entity.Portfolio;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import com.nithiwut.wealthhub.transaction.dto.request.CreateTransactionRequest;
import com.nithiwut.wealthhub.transaction.dto.response.TransactionResponse;
import com.nithiwut.wealthhub.transaction.entity.InvestmentTransaction;
import com.nithiwut.wealthhub.transaction.entity.TransactionType;
import com.nithiwut.wealthhub.transaction.repository.InvestmentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentTransactionServiceTest {
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private HoldingRepository holdingRepository;
    @Mock private InvestmentTransactionRepository transactionRepository;

    private InvestmentTransactionService service;
    private Portfolio portfolio;
    private Asset asset;

    @BeforeEach
    void setUp() {
        service = new InvestmentTransactionService(
            portfolioRepository, assetRepository, holdingRepository, transactionRepository);
        portfolio = Portfolio.builder().id(1L).name("Retirement").baseCurrency("THB").build();
        asset = Asset.builder().id(2L).symbol("AAPL").name("Apple").market("NASDAQ")
            .assetType("STOCK").currency("USD").build();
        lenient().when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void firstBuyCreatesHolding() {
        prepareExistingPortfolioAndAsset();
        when(holdingRepository.applyBuy(1L, 2L, new BigDecimal("10"), new BigDecimal("100")))
            .thenReturn(0);

        service.createTransaction(request(TransactionType.BUY, "10", "100"));

        ArgumentCaptor<Holding> holdingCaptor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).saveAndFlush(holdingCaptor.capture());
        assertThat(holdingCaptor.getValue().getQuantity()).isEqualByComparingTo("10");
        assertThat(holdingCaptor.getValue().getAverageCost()).isEqualByComparingTo("100");
        verify(transactionRepository).save(any(InvestmentTransaction.class));
    }

    @Test
    void secondBuyUsesAtomicWeightedAverageUpdate() {
        prepareExistingPortfolioAndAsset();
        when(holdingRepository.applyBuy(1L, 2L, new BigDecimal("5"), new BigDecimal("130")))
            .thenReturn(1);

        service.createTransaction(request(TransactionType.BUY, "5", "130"));

        verify(holdingRepository).applyBuy(1L, 2L, new BigDecimal("5"), new BigDecimal("130"));
        verify(holdingRepository, never()).saveAndFlush(any());
    }

    @Test
    void sellUsesAtomicConditionalUpdateAndChecksForClosedPosition() {
        prepareExistingPortfolioAndAsset();
        when(holdingRepository.applySell(1L, 2L, new BigDecimal("5"))).thenReturn(Optional.of(new BigDecimal("110")));

        service.createTransaction(request(TransactionType.SELL, "5", "150"));

        verify(holdingRepository).applySell(1L, 2L, new BigDecimal("5"));
        verify(holdingRepository).deleteClosedPosition(1L, 2L);
        verify(transactionRepository).save(any(InvestmentTransaction.class));
    }

    @Test
    void sellEntirePositionDeletesHolding() {
        prepareExistingPortfolioAndAsset();
        when(holdingRepository.applySell(1L, 2L, new BigDecimal("5"))).thenReturn(Optional.of(new BigDecimal("110")));
        when(holdingRepository.deleteClosedPosition(1L, 2L)).thenReturn(1);

        service.createTransaction(request(TransactionType.SELL, "5", "150"));

        InOrder mutationBeforeLedger = inOrder(holdingRepository, transactionRepository);
        mutationBeforeLedger.verify(holdingRepository).applySell(1L, 2L, new BigDecimal("5"));
        mutationBeforeLedger.verify(holdingRepository).deleteClosedPosition(1L, 2L);
        mutationBeforeLedger.verify(transactionRepository).save(any(InvestmentTransaction.class));
    }

    @Test
    void sellGreaterThanAvailableIsRejectedWithoutSavingTransaction() {
        prepareExistingPortfolioAndAsset();
        when(holdingRepository.applySell(1L, 2L, new BigDecimal("6"))).thenReturn(Optional.empty());
        when(holdingRepository.existsByPortfolioIdAndAssetId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.createTransaction(request(TransactionType.SELL, "6", "120")))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.INSUFFICIENT_HOLDING_QUANTITY);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void sellWithoutHoldingIsRejected() {
        prepareExistingPortfolioAndAsset();
        when(holdingRepository.applySell(1L, 2L, BigDecimal.ONE)).thenReturn(Optional.empty());
        when(holdingRepository.existsByPortfolioIdAndAssetId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> service.createTransaction(request(TransactionType.SELL, "1", "120")))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.HOLDING_NOT_FOUND);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void competingFirstBuyFailsCleanlyWithoutRecordingTransaction() {
        prepareExistingPortfolioAndAsset();
        when(holdingRepository.applyBuy(1L, 2L, BigDecimal.ONE, new BigDecimal("100"))).thenReturn(0);
        when(holdingRepository.saveAndFlush(any(Holding.class)))
            .thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> service.createTransaction(request(TransactionType.BUY, "1", "100")))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_HOLDING);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void invalidQuantityIsRejectedBeforeAnyWrite() {
        assertThatThrownBy(() -> service.createTransaction(request(TransactionType.BUY, "0", "100")))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REQUEST);
        verify(holdingRepository, never()).applyBuy(any(), any(), any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void invalidPriceIsRejectedBeforeAnyWrite() {
        assertThatThrownBy(() -> service.createTransaction(request(TransactionType.BUY, "1", "-1")))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REQUEST);
        verify(holdingRepository, never()).applyBuy(any(), any(), any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void nonexistentPortfolioIsRejected() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTransaction(request(TransactionType.BUY, "1", "100")))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.PORTFOLIO_NOT_FOUND);
        verify(assetRepository, never()).findById(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void nonexistentAssetIsRejected() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTransaction(request(TransactionType.BUY, "1", "100")))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.ASSET_NOT_FOUND);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void historyIsReturnedInRepositoryOrder() {
        LocalDateTime newestTime = LocalDateTime.of(2026, 9, 23, 12, 0);
        InvestmentTransaction newest = transaction(12L, newestTime);
        InvestmentTransaction older = transaction(11L, newestTime.minusDays(1));
        when(portfolioRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findHistoryByPortfolioId(1L)).thenReturn(List.of(newest, older));

        List<TransactionResponse> history = service.getTransactionHistory(1L);

        assertThat(history).extracting(TransactionResponse::id).containsExactly(12L, 11L);
        verify(transactionRepository).findHistoryByPortfolioId(1L);
    }

    @Test
    void historyForNonexistentPortfolioIsRejected() {
        when(portfolioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getTransactionHistory(99L))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.PORTFOLIO_NOT_FOUND);
        verify(transactionRepository, never()).findHistoryByPortfolioId(any());
    }

    private void prepareExistingPortfolioAndAsset() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findById(2L)).thenReturn(Optional.of(asset));
    }

    private CreateTransactionRequest request(TransactionType type, String quantity, String price) {
        return new CreateTransactionRequest(
            1L, 2L, type, new BigDecimal(quantity), new BigDecimal(price));
    }

    private InvestmentTransaction transaction(Long id, LocalDateTime createdAt) {
        return InvestmentTransaction.builder().id(id).portfolio(portfolio).asset(asset)
            .type(TransactionType.BUY).quantity(BigDecimal.ONE).price(BigDecimal.TEN)
            .createdAt(createdAt).build();
    }
}
