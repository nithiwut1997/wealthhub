package com.nithiwut.wealthhub.asset.dto.response;

public record AssetResponse(
    Long id,
    String symbol,
    String name,
    String market,
    String assetType,
    String currency,
    Boolean isActive
) {
}
