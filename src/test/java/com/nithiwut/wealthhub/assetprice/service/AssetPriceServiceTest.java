package com.nithiwut.wealthhub.assetprice.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.assetprice.dto.request.CreateAssetPriceRequest;
import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetPriceServiceTest {
    @Mock private AssetRepository assetRepository;
    @Mock private AssetPriceRepository priceRepository;
    private AssetPriceService service;
    private Asset asset;

    @BeforeEach
    void setUp() {
        service = new AssetPriceService(assetRepository, priceRepository);
        asset = Asset.builder().id(4L).symbol("PTT").currency("THB").build();
    }

    @Test
    void createsAndStoresHistoricalAssetPrice() {
        LocalDateTime pricedAt = LocalDateTime.of(2026, 9, 23, 10, 0);
        when(assetRepository.findById(4L)).thenReturn(Optional.of(asset));
        when(priceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createAssetPrice(
            4L, new CreateAssetPriceRequest(new BigDecimal("520.50"), pricedAt));

        ArgumentCaptor<AssetPrice> captor = ArgumentCaptor.forClass(AssetPrice.class);
        verify(priceRepository).save(captor.capture());
        assertThat(captor.getValue().getAsset()).isSameAs(asset);
        assertThat(captor.getValue().getPrice()).isEqualByComparingTo("520.50");
        assertThat(captor.getValue().getPricedAt()).isEqualTo(pricedAt);
        assertThat(response.price()).isEqualByComparingTo("520.50");
    }

    @Test
    void rejectsNonPositivePriceBeforeLookingUpAsset() {
        assertThatThrownBy(() -> service.createAssetPrice(
            4L, new CreateAssetPriceRequest(BigDecimal.ZERO, LocalDateTime.now())))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REQUEST);
        verify(assetRepository, never()).findById(any());
        verify(priceRepository, never()).save(any());
    }

    @Test
    void rejectsNonexistentAsset() {
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAssetPrice(
            99L, new CreateAssetPriceRequest(BigDecimal.ONE, LocalDateTime.now())))
            .isInstanceOf(ApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.ASSET_NOT_FOUND);
        verify(priceRepository, never()).save(any());
    }

    @Test
    void returnsLatestPriceSelectedByRepository() {
        AssetPrice latest = AssetPrice.builder().id(8L).asset(asset)
            .price(new BigDecimal("150")).pricedAt(LocalDateTime.now()).build();
        when(assetRepository.existsById(4L)).thenReturn(true);
        when(priceRepository.findFirstByAssetIdOrderByPricedAtDescIdDesc(4L))
            .thenReturn(Optional.of(latest));

        assertThat(service.getLatestAssetPrice(4L).id()).isEqualTo(8L);
        verify(priceRepository).findFirstByAssetIdOrderByPricedAtDescIdDesc(4L);
    }
}
