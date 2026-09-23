package com.nithiwut.wealthhub.assetprice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssetPriceResponse(Long id, Long assetId, BigDecimal price, LocalDateTime pricedAt) {
}
