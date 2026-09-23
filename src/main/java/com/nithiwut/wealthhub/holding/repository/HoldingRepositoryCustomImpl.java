package com.nithiwut.wealthhub.holding.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HoldingRepositoryCustomImpl implements HoldingRepositoryCustom {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<BigDecimal> applySell(Long portfolioId, Long assetId, BigDecimal sellQuantity) {
        List<BigDecimal> averageCosts = jdbcTemplate.query("""
                UPDATE holding
                SET quantity = quantity - :sellQuantity,
                    updated_at = CURRENT_TIMESTAMP
                WHERE portfolio_id = :portfolioId
                  AND asset_id = :assetId
                  AND quantity >= :sellQuantity
                RETURNING average_cost
                """,
            new MapSqlParameterSource()
                .addValue("portfolioId", portfolioId)
                .addValue("assetId", assetId)
                .addValue("sellQuantity", sellQuantity),
            (resultSet, rowNum) -> resultSet.getBigDecimal("average_cost"));

        return averageCosts.stream().findFirst();
    }
}
