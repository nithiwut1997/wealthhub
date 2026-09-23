package com.nithiwut.wealthhub.holding.repository;

import com.nithiwut.wealthhub.holding.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    boolean existsByPortfolioIdAndAssetId(Long portfolioId, Long assetId);

    Optional<Holding> findByPortfolioIdAndAssetId(Long portfolioId, Long assetId);

    List<Holding> findByPortfolioId(Long portfolioId);

    @Query("SELECT COALESCE(SUM(h.quantity * h.averageCost), 0) FROM Holding h")
    BigDecimal getTotalHoldingCost();
}
