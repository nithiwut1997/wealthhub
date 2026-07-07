package com.nithiwut.wealthhub.asset.dto.request;

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

    @NotBlank
    @Size(max = 20)
    String assetType,

    @NotBlank
    @Size(min = 3, max = 3)
    String currency
) {
}
