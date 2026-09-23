package com.nithiwut.wealthhub.holding.controller;

import com.nithiwut.wealthhub.holding.dto.response.HoldingResponse;
import com.nithiwut.wealthhub.holding.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/holdings")
@RequiredArgsConstructor
public class HoldingController {
    private final HoldingService holdingService;

    @GetMapping
    public List<HoldingResponse> getHoldingByPortfolioId(@RequestParam Long portfolioId) {
        return holdingService.getHoldingsByPortfolioId(portfolioId);
    }
}
