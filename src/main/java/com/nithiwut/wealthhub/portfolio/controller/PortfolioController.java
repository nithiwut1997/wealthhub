package com.nithiwut.wealthhub.portfolio.controller;

import com.nithiwut.wealthhub.portfolio.dto.request.CreatePortfolioRequest;
import com.nithiwut.wealthhub.portfolio.dto.response.PortfolioResponse;
import com.nithiwut.wealthhub.portfolio.dto.response.PortfolioSummaryResponse;
import com.nithiwut.wealthhub.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioResponse createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        return portfolioService.createPortfolio(request);
    }

    @GetMapping("/{id}")
    public PortfolioResponse getPortfolioById(@PathVariable Long id) {
        return portfolioService.getPortfolioById(id);
    }

    @GetMapping
    public List<PortfolioResponse> getAllPortfolios() {
        return portfolioService.getAllPortfolios();
    }

    @GetMapping("/{id}/summary")
    public PortfolioSummaryResponse getPortfolioSummary(@PathVariable Long id) {
        return portfolioService.getPortfolioSummary(id);
    }
}
