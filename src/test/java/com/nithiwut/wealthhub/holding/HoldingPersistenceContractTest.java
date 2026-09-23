package com.nithiwut.wealthhub.holding;

import com.nithiwut.wealthhub.holding.entity.Holding;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HoldingPersistenceContractTest {

    @Test
    void quantityAndAverageCostMatchDatabasePrecision() throws NoSuchFieldException {
        Column quantity = Holding.class.getDeclaredField("quantity").getAnnotation(Column.class);
        Column averageCost = Holding.class.getDeclaredField("averageCost").getAnnotation(Column.class);

        assertThat(quantity.precision()).isEqualTo(20);
        assertThat(quantity.scale()).isEqualTo(8);
        assertThat(averageCost.precision()).isEqualTo(20);
        assertThat(averageCost.scale()).isEqualTo(8);
    }

    @Test
    void buyUpdateCalculatesWeightedAverageFromCurrentDatabaseValues() throws NoSuchMethodException {
        String sql = queryFor("applyBuy", Long.class, Long.class, BigDecimal.class, BigDecimal.class);

        assertThat(normalize(sql))
            .contains("(quantity * average_cost) + (:buyQuantity * :buyPrice)")
            .contains("/ (quantity + :buyQuantity)")
            .contains("quantity = quantity + :buyQuantity")
            .contains("ROUND(")
            .contains(", 8");
    }

    @Test
    void sellUpdateIsConditionalAndCannotMakeQuantityNegative() throws NoSuchMethodException {
        String sql = queryFor("applySell", Long.class, Long.class, BigDecimal.class);

        assertThat(normalize(sql))
            .contains("quantity = quantity - :sellQuantity")
            .contains("quantity >= :sellQuantity")
            .doesNotContain("average_cost");
    }

    @Test
    void closedPositionDeleteOnlyRemovesZeroQuantityHolding() throws NoSuchMethodException {
        String sql = queryFor("deleteClosedPosition", Long.class, Long.class);

        assertThat(normalize(sql)).contains("DELETE FROM holding").contains("quantity = 0");
    }

    @Test
    void uniquenessMigrationTargetsPortfolioAndAssetAndHasRollback() throws Exception {
        String migration;
        try (var stream = getClass().getResourceAsStream(
            "/db/changelog/changes/006-add-holding-portfolio-asset-unique-constraint.sql")) {
            assertThat(stream).isNotNull();
            migration = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(normalize(migration))
            .contains("CONSTRAINT uk_holding_portfolio_asset UNIQUE (portfolio_id, asset_id)")
            .contains("-- rollback ALTER TABLE holding DROP CONSTRAINT uk_holding_portfolio_asset;");
    }

    private String queryFor(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = HoldingRepository.class.getMethod(name, parameterTypes);
        Query query = method.getAnnotation(Query.class);
        assertThat(query).isNotNull();
        assertThat(query.nativeQuery()).isTrue();
        return query.value();
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
