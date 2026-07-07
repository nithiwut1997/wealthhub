package com.nithiwut.wealthhub.holding.repository;

import com.nithiwut.wealthhub.holding.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    boolean existsByPortfolioIdAndAssetId(Long portfolioId, Long assetId);

    List<Holding> findByPortfolioId(Long portfolioId);
}
