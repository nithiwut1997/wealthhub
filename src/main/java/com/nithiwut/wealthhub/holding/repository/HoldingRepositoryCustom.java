package com.nithiwut.wealthhub.holding.repository;

import java.math.BigDecimal;
import java.util.Optional;

public interface HoldingRepositoryCustom {
    Optional<BigDecimal> applySell(Long portfolioId, Long assetId, BigDecimal sellQuantity);
}
