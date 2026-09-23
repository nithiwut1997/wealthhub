package com.nithiwut.wealthhub.transaction.repository;

import com.nithiwut.wealthhub.transaction.entity.InvestmentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, Long> {
    @Query("""
        SELECT t FROM InvestmentTransaction t
        JOIN FETCH t.asset
        WHERE t.portfolio.id = :portfolioId
        ORDER BY t.createdAt DESC, t.id DESC
        """)
    List<InvestmentTransaction> findHistoryByPortfolioId(@Param("portfolioId") Long portfolioId);
}
