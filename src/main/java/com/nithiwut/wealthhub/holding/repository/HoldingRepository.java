package com.nithiwut.wealthhub.holding.repository;

import com.nithiwut.wealthhub.holding.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    boolean existsByPortfolioIdAndAssetId(Long portfolioId, Long assetId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
        UPDATE holding
        SET average_cost = ROUND(
                ((quantity * average_cost) + (:buyQuantity * :buyPrice))
                    / (quantity + :buyQuantity),
                8
            ),
            quantity = quantity + :buyQuantity,
            updated_at = CURRENT_TIMESTAMP
        WHERE portfolio_id = :portfolioId AND asset_id = :assetId
        """, nativeQuery = true)
    int applyBuy(
        @Param("portfolioId") Long portfolioId,
        @Param("assetId") Long assetId,
        @Param("buyQuantity") BigDecimal buyQuantity,
        @Param("buyPrice") BigDecimal buyPrice
    );

    @Modifying(flushAutomatically = true)
    @Query(value = """
        UPDATE holding
        SET quantity = quantity - :sellQuantity,
            updated_at = CURRENT_TIMESTAMP
        WHERE portfolio_id = :portfolioId
          AND asset_id = :assetId
          AND quantity >= :sellQuantity
        """, nativeQuery = true)
    int applySell(
        @Param("portfolioId") Long portfolioId,
        @Param("assetId") Long assetId,
        @Param("sellQuantity") BigDecimal sellQuantity
    );

    @Modifying(flushAutomatically = true)
    @Query(value = """
        DELETE FROM holding
        WHERE portfolio_id = :portfolioId
          AND asset_id = :assetId
          AND quantity = 0
        """, nativeQuery = true)
    int deleteClosedPosition(
        @Param("portfolioId") Long portfolioId,
        @Param("assetId") Long assetId
    );

    List<Holding> findByPortfolioId(Long portfolioId);

    @Query("SELECT COALESCE(SUM(h.quantity * h.averageCost), 0) FROM Holding h")
    BigDecimal getTotalHoldingCost();
}
