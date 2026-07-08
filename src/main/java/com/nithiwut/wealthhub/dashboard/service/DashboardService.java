package com.nithiwut.wealthhub.dashboard.service;

import com.nithiwut.wealthhub.asset.repository.AssetRepository;
import com.nithiwut.wealthhub.dashboard.dto.response.DashboardResponse;
import com.nithiwut.wealthhub.holding.repository.HoldingRepository;
import com.nithiwut.wealthhub.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
            portfolioRepository.count(),
            holdingRepository.count(),
            assetRepository.count(),
            holdingRepository.getTotalHoldingCost()
        );
    }
}
