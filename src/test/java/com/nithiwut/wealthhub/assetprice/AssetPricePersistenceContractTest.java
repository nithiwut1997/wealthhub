package com.nithiwut.wealthhub.assetprice;

import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class AssetPricePersistenceContractTest {
    @Test
    void priceUsesProjectFinancialPrecision() throws Exception {
        Column price = AssetPrice.class.getDeclaredField("price").getAnnotation(Column.class);
        assertThat(price.precision()).isEqualTo(20);
        assertThat(price.scale()).isEqualTo(8);
    }

    @Test
    void singleAssetLookupOrdersByTimestampThenId() throws Exception {
        assertThat(AssetPriceRepository.class
            .getMethod("findFirstByAssetIdOrderByPricedAtDescIdDesc", Long.class))
            .isNotNull();
    }

    @Test
    void batchLookupUsesOnePostgresLatestPerAssetQueryWithDeterministicTieBreak() throws Exception {
        Query query = AssetPriceRepository.class.getMethod("findLatestByAssetIds", Collection.class)
            .getAnnotation(Query.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(normalize(query.value()))
            .contains("DISTINCT ON (ap.asset_id)")
            .contains("WHERE ap.asset_id IN (:assetIds)")
            .contains("ORDER BY ap.asset_id, ap.priced_at DESC, ap.id DESC");
    }

    @Test
    void historyMigrationHasSupportingIndexConstraintAndRollback() throws Exception {
        String migration;
        try (var stream = getClass().getResourceAsStream(
            "/db/changelog/changes/007-enable-asset-price-history.sql")) {
            assertThat(stream).isNotNull();
            migration = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertThat(normalize(migration))
            .contains("CHECK (price > 0)")
            .contains("ON asset_price(asset_id, priced_at DESC, id DESC)")
            .contains("-- rollback DROP INDEX idx_asset_price_latest;");
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
