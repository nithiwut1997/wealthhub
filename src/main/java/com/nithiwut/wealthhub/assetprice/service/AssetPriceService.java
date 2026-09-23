package com.nithiwut.wealthhub.assetprice.service;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.assetprice.dto.request.CreateAssetPriceRequest;
import com.nithiwut.wealthhub.assetprice.dto.response.AssetPriceResponse;
import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetPriceService {
    private final AssetRepository assetRepository;
    private final AssetPriceRepository assetPriceRepository;

    @Transactional
    public AssetPriceResponse createAssetPrice(Long assetId, CreateAssetPriceRequest request) {
        if (request.price() == null || request.price().compareTo(java.math.BigDecimal.ZERO) <= 0) {
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

    private AssetPriceResponse toResponse(AssetPrice price) {
        return new AssetPriceResponse(
            price.getId(), price.getAsset().getId(), price.getPrice(), price.getPricedAt());
    }
}
