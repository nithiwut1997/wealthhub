package com.nithiwut.wealthhub.marketdata;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketPrice(BigDecimal price, LocalDateTime pricedAt) {
}
