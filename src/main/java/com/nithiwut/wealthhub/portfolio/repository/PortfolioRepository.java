package com.nithiwut.wealthhub.portfolio.repository;

import com.nithiwut.wealthhub.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
