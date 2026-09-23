package com.nithiwut.wealthhub.assetprice.repository;

import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssetPriceRepository extends JpaRepository<AssetPrice, Long> {
    Optional<AssetPrice> findFirstByAssetIdOrderByPricedAtDescIdDesc(Long assetId);

    Optional<AssetPrice> findFirstByAssetIdAndPricedAtOrderByIdDesc(Long assetId, LocalDateTime pricedAt);

    @Query(value = """
        SELECT DISTINCT ON (ap.asset_id) ap.*
        FROM asset_price ap
        WHERE ap.asset_id IN (:assetIds)
        ORDER BY ap.asset_id, ap.priced_at DESC, ap.id DESC
        """, nativeQuery = true)
    List<AssetPrice> findLatestByAssetIds(@Param("assetIds") Collection<Long> assetIds);
}
