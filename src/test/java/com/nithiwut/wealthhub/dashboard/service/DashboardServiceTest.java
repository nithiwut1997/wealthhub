package com.nithiwut.wealthhub.dashboard.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private HoldingRepository holdingRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private AssetPriceRepository priceRepository;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(
            portfolioRepository, holdingRepository, assetRepository, priceRepository);
    }

    @Test
    void aggregatesMultiplePortfoliosWithPartialValuationAndEmptyBreakdown() {
        Portfolio core = portfolio(1L, "Core");
        Portfolio growth = portfolio(2L, "Growth");
        Portfolio empty = portfolio(3L, "Empty");
        Asset ptt = asset(10L, "PTT");
        Asset cpall = asset(11L, "CPALL");
        Asset kbank = asset(12L, "KBANK");
        Holding pricedCore = holding(core, ptt, "10", "100");
        Holding unpricedCore = holding(core, cpall, "5", "200");
        Holding pricedGrowth = holding(growth, kbank, "20", "50");
        when(portfolioRepository.findAll()).thenReturn(List.of(core, growth, empty));
        when(holdingRepository.findAllWithPortfolioAndAsset())
            .thenReturn(List.of(pricedCore, unpricedCore, pricedGrowth));
        when(priceRepository.findLatestByAssetIds(List.of(10L, 11L, 12L)))
            .thenReturn(List.of(price(ptt, "120"), price(kbank, "40")));
        when(assetRepository.count()).thenReturn(3L);

        var dashboard = service.getDashboard();

        assertThat(dashboard.baseCurrency()).isEqualTo("THB");
        assertThat(dashboard.portfolioCount()).isEqualTo(3L);
        assertThat(dashboard.holdingCount()).isEqualTo(3L);
        assertThat(dashboard.assetCount()).isEqualTo(3L);
        assertThat(dashboard.totalCost()).isEqualByComparingTo("3000.00000000");
        assertThat(dashboard.totalMarketValue()).isEqualByComparingTo("2000.00000000");
        assertThat(dashboard.unrealizedGainLoss()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dashboard.unrealizedGainLossPercent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dashboard.pricedHoldingCount()).isEqualTo(2L);
        assertThat(dashboard.missingPriceCount()).isEqualTo(1L);
        assertThat(dashboard.portfolios()).hasSize(3);
        assertThat(dashboard.portfolios().get(0).totalCost()).isEqualByComparingTo("2000.00000000");
        assertThat(dashboard.portfolios().get(0).unrealizedGainLoss())
            .isEqualByComparingTo("200.00000000");
        assertThat(dashboard.portfolios().get(0).unrealizedGainLossPercent())
            .isEqualByComparingTo("20.0000");
        assertThat(dashboard.portfolios().get(0).missingPriceCount()).isEqualTo(1);
        assertThat(dashboard.portfolios().get(1).unrealizedGainLoss())
            .isEqualByComparingTo("-200.00000000");
        assertThat(dashboard.portfolios().get(2).holdingCount()).isZero();
        assertThat(dashboard.portfolios().get(2).totalMarketValue())
            .isEqualByComparingTo(BigDecimal.ZERO);
        verify(priceRepository).findLatestByAssetIds(List.of(10L, 11L, 12L));
    }

    @Test
    void noPortfoliosReturnsEmptyDashboardWithoutPriceQuery() {
        when(portfolioRepository.findAll()).thenReturn(List.of());
        when(holdingRepository.findAllWithPortfolioAndAsset()).thenReturn(List.of());
        when(assetRepository.count()).thenReturn(0L);

        var dashboard = service.getDashboard();

        assertThat(dashboard.portfolioCount()).isZero();
        assertThat(dashboard.totalCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dashboard.totalMarketValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dashboard.unrealizedGainLossPercent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dashboard.portfolios()).isEmpty();
        verify(priceRepository, never()).findLatestByAssetIds(anyCollection());
    }

    @Test
    void allMissingPricesKeepCostAndReturnZeroValuation() {
        Portfolio portfolio = portfolio(1L, "Core");
        Asset asset = asset(10L, "PTT");
        when(portfolioRepository.findAll()).thenReturn(List.of(portfolio));
        when(holdingRepository.findAllWithPortfolioAndAsset())
            .thenReturn(List.of(holding(portfolio, asset, "10", "100")));
        when(priceRepository.findLatestByAssetIds(List.of(10L))).thenReturn(List.of());
        when(assetRepository.count()).thenReturn(1L);

        var dashboard = service.getDashboard();

        assertThat(dashboard.totalCost()).isEqualByComparingTo("1000.00000000");
        assertThat(dashboard.totalMarketValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dashboard.unrealizedGainLoss()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dashboard.unrealizedGainLossPercent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dashboard.pricedHoldingCount()).isZero();
        assertThat(dashboard.missingPriceCount()).isEqualTo(1L);
    }

    private Portfolio portfolio(Long id, String name) {
        return Portfolio.builder().id(id).name(name).baseCurrency("THB").build();
    }

    private Asset asset(Long id, String symbol) {
        return Asset.builder().id(id).symbol(symbol).currency("THB").build();
    }

    private Holding holding(Portfolio portfolio, Asset asset, String quantity, String averageCost) {
        return Holding.builder().portfolio(portfolio).asset(asset)
            .quantity(new BigDecimal(quantity)).averageCost(new BigDecimal(averageCost)).build();
    }

    private AssetPrice price(Asset asset, String value) {
        return AssetPrice.builder().asset(asset).price(new BigDecimal(value))
            .pricedAt(LocalDateTime.of(2026, 9, 23, 10, 0)).build();
    }
}
