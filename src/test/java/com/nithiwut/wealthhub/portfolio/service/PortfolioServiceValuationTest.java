package com.nithiwut.wealthhub.portfolio.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.ApiException;
import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.entity.Portfolio;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceValuationTest {
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private HoldingRepository holdingRepository;
    @Mock private AssetPriceRepository priceRepository;

    private PortfolioService service;
    private Portfolio portfolio;
    private Asset asset;
    private Holding holding;

    @BeforeEach
    void setUp() {
        service = new PortfolioService(portfolioRepository, holdingRepository, priceRepository);
        portfolio = Portfolio.builder().id(1L).name("Core").baseCurrency("THB").build();
        asset = Asset.builder().id(2L).symbol("PTT").currency("THB").build();
        holding = Holding.builder().id(3L).portfolio(portfolio).asset(asset)
            .quantity(new BigDecimal("15")).averageCost(new BigDecimal("110")).build();
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
    }

    @Test
    void valuesPortfolioFromLatestPricesAndDerivesUnrealizedProfit() {
        AssetPrice price = price(asset, 10L, "150");
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(List.of(holding));
        when(priceRepository.findLatestByAssetIds(List.of(2L))).thenReturn(List.of(price));

        var summary = service.getPortfolioSummary(1L);

        assertThat(summary.totalCost()).isEqualByComparingTo("1650.00000000");
        assertThat(summary.totalMarketValue()).isEqualByComparingTo("2250.00000000");
        assertThat(summary.unrealizedGainLoss()).isEqualByComparingTo("600.00000000");
        assertThat(summary.unrealizedGainLossPercent()).isEqualByComparingTo("36.3600");
    }

    @Test
    void failsExplicitlyWhenAHoldingHasNoPrice() {
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(List.of(holding));
        when(priceRepository.findLatestByAssetIds(List.of(2L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.getPortfolioSummary(1L))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("PTT")
            .extracting("errorCode").isEqualTo(ErrorCode.ASSET_PRICE_NOT_FOUND);
    }

    @Test
    void rejectsMixedCurrencyRatherThanAddingIncompatibleValues() {
        Asset usdAsset = Asset.builder().id(4L).symbol("AAPL").currency("USD").build();
        Holding usdHolding = Holding.builder().portfolio(portfolio).asset(usdAsset)
            .quantity(BigDecimal.ONE).averageCost(new BigDecimal("100")).build();
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(List.of(usdHolding));
        when(priceRepository.findLatestByAssetIds(List.of(4L)))
            .thenReturn(List.of(price(usdAsset, 11L, "150")));

        assertThatThrownBy(() -> service.getPortfolioSummary(1L))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("FX conversion is not supported")
            .extracting("errorCode").isEqualTo(ErrorCode.UNSUPPORTED_CURRENCY);
    }

    @Test
    void loadsAllLatestPricesInOneBatchAndDoesNotMutateHolding() {
        Asset secondAsset = Asset.builder().id(5L).symbol("CPALL").currency("THB").build();
        Holding secondHolding = Holding.builder().portfolio(portfolio).asset(secondAsset)
            .quantity(BigDecimal.TEN).averageCost(new BigDecimal("60")).build();
        BigDecimal originalQuantity = holding.getQuantity();
        BigDecimal originalAverageCost = holding.getAverageCost();
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(List.of(holding, secondHolding));
        when(priceRepository.findLatestByAssetIds(List.of(2L, 5L)))
            .thenReturn(List.of(price(asset, 10L, "150"), price(secondAsset, 12L, "70")));

        service.getPortfolioSummary(1L);

        verify(priceRepository).findLatestByAssetIds(List.of(2L, 5L));
        verify(holdingRepository, never()).save(holding);
        assertThat(holding.getQuantity()).isSameAs(originalQuantity);
        assertThat(holding.getAverageCost()).isSameAs(originalAverageCost);
    }

    @Test
    void emptyPortfolioHasZeroTotalsWithoutPriceQuery() {
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(List.of());

        var summary = service.getPortfolioSummary(1L);

        assertThat(summary.totalCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.totalMarketValue()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(priceRepository, never()).findLatestByAssetIds(anyCollection());
    }

    private AssetPrice price(Asset pricedAsset, Long id, String value) {
        return AssetPrice.builder().id(id).asset(pricedAsset).price(new BigDecimal(value))
            .pricedAt(LocalDateTime.of(2026, 9, 23, 10, 0)).build();
    }
}
