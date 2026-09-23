package com.nithiwut.wealthhub.asset.dto.response;

import com.nithiwut.wealthhub.asset.entity.AssetType;

public record AssetResponse(
    Long id,
    String symbol,
    String name,
    String market,
    AssetType type,
    String currency,
    String externalId,
    Boolean isActive
) {
}
