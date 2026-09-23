package com.nithiwut.wealthhub.asset.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.nithiwut.wealthhub.asset.entity.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAssetRequest(
    @NotBlank
    @Size(max = 30)
    String symbol,

    @NotBlank
    @Size(max = 255)
    String name,

    @NotBlank
    @Size(max = 20)
    String market,

    @JsonAlias("assetType")
    AssetType type,

    @NotBlank
    @Size(min = 3, max = 3)
    String currency,

    @Size(max = 100)
    String externalId
) {
}
