package com.nithiwut.wealthhub.assetprice;

import com.nithiwut.wealthhub.assetprice.entity.AssetPrice;
import com.nithiwut.wealthhub.assetprice.repository.AssetPriceRepository;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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
    void entityColumnNamesMatchInitialMigration() throws Exception {
        Table table = AssetPrice.class.getAnnotation(Table.class);
        JoinColumn asset = AssetPrice.class.getDeclaredField("asset").getAnnotation(JoinColumn.class);
        Column pricedAt = AssetPrice.class.getDeclaredField("pricedAt").getAnnotation(Column.class);
        Column createdAt = AssetPrice.class.getDeclaredField("createdAt").getAnnotation(Column.class);

        assertThat(table.name()).isEqualTo("asset_price");
        assertThat(asset.name()).isEqualTo("asset_id");
        assertThat(pricedAt.name()).isEqualTo("priced_at");
        assertThat(createdAt.name()).isEqualTo("created_at");
        assertThat(createdAt.updatable()).isFalse();
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
    void initialAssetPriceMigrationCreatesHistorySchemaWithIndexConstraintAndRollback() throws Exception {
        String migration;
        try (var stream = getClass().getResourceAsStream(
            "/db/changelog/changes/004-create-asset-price.sql")) {
            assertThat(stream).isNotNull();
            migration = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertThat(normalize(migration))
            .contains("id BIGSERIAL PRIMARY KEY")
            .contains("asset_id BIGINT NOT NULL")
            .contains("price NUMERIC(20,8) NOT NULL")
            .contains("priced_at TIMESTAMP NOT NULL")
            .contains("created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
            .contains("CHECK (price > 0)")
            .contains("ON asset_price(asset_id, priced_at DESC, id DESC)")
            .contains("-- rollback DROP TABLE asset_price;")
            .doesNotContain("asset_id BIGINT PRIMARY KEY");
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
