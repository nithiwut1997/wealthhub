package com.nithiwut.wealthhub.asset.service;

import com.nithiwut.wealthhub.asset.dto.request.CreateAssetRequest;
import com.nithiwut.wealthhub.asset.dto.response.AssetResponse;
import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.ConflictException;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;

    @Transactional
    public AssetResponse createAsset(CreateAssetRequest request) {
        String symbol = normalizeText(request.symbol());
        if (assetRepository.existsBySymbolAndMarket(symbol, request.market())) {
            throw new ConflictException(ErrorCode.DUPLICATE_ASSET, "Asset with the same symbol and market already exists");
        }

        Asset asset = Asset.builder()
                .symbol(symbol)
                .name(request.name())
                .market(request.market())
                .assetType(request.assetType())
                .currency(normalizeText(request.currency()))
                .build();
        Asset savedAsset = assetRepository.save(asset);
        return toResponse(savedAsset);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASSET_NOT_FOUND, "Asset not found"));
        return toResponse(asset);
    }

    private AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getSymbol(),
                asset.getName(),
                asset.getMarket(),
                asset.getAssetType(),
                asset.getCurrency(),
                asset.getIsActive()
        );
    }

    private String normalizeText(String text) {
        return text.toUpperCase().trim();
    }
}
