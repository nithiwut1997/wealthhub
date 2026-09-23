package com.nithiwut.wealthhub.assetprice.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.entity.AssetType;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.assetprice.dto.request.CreateAssetPriceRequest;
import com.nithiwut.wealthhub.assetprice.dto.response.AssetPriceResponse;
import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.marketdata.MarketDataProvider;
import com.nithiwut.wealthhub.marketdata.MarketDataProviderResolver;
import com.nithiwut.wealthhub.marketdata.MarketPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AssetPriceService {
    private final AssetRepository assetRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final MarketDataProviderResolver marketDataProviderResolver;

    @Transactional
    public AssetPriceResponse createAssetPrice(Long assetId, CreateAssetPriceRequest request) {
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than zero");
        }
        if (request.pricedAt() == null) {
            throw new BadRequestException("Priced at must not be null");
        }

        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.ASSET_NOT_FOUND, "Asset not found"));
        AssetPrice saved = assetPriceRepository.save(AssetPrice.builder()
            .asset(asset)
            .price(request.price())
            .pricedAt(request.pricedAt())
            .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AssetPriceResponse getLatestAssetPrice(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new NotFoundException(ErrorCode.ASSET_NOT_FOUND, "Asset not found");
        }
        AssetPrice price = assetPriceRepository.findFirstByAssetIdOrderByPricedAtDescIdDesc(assetId)
            .orElseThrow(() -> new NotFoundException(
                ErrorCode.ASSET_PRICE_NOT_FOUND, "No market price found for asset id: " + assetId));
        return toResponse(price);
    }

    @Transactional
    public AssetPriceResponse refreshAssetPrice(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.ASSET_NOT_FOUND, "Asset not found"));
        if (asset.getType() != AssetType.MUTUAL_FUND) {
            throw new BadRequestException("Price refresh only supports MUTUAL_FUND assets");
        }

        MarketDataProvider provider = marketDataProviderResolver.resolve(asset.getType());
        MarketPrice marketPrice = provider.getLatestPrice(asset);
        AssetPrice price = assetPriceRepository
            .findFirstByAssetIdAndPricedAtOrderByIdDesc(assetId, marketPrice.pricedAt())
            .filter(existing -> existing.getPrice().compareTo(marketPrice.price()) == 0)
            .orElseGet(() -> assetPriceRepository.save(AssetPrice.builder()
                .asset(asset)
                .price(marketPrice.price())
                .pricedAt(marketPrice.pricedAt())
                .build()));
        return toResponse(price);
    }

    private AssetPriceResponse toResponse(AssetPrice price) {
        return new AssetPriceResponse(
            price.getId(), price.getAsset().getId(), price.getPrice(), price.getPricedAt());
    }
}
