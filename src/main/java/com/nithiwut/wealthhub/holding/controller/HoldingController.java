package com.nithiwut.wealthhub.holding.controller;

import com.nithiwut.wealthhub.holding.dto.request.CreateHoldingRequest;
import com.nithiwut.wealthhub.holding.dto.response.HoldingResponse;
import com.nithiwut.wealthhub.holding.service.HoldingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/holdings")
@RequiredArgsConstructor
public class HoldingController {
    private final HoldingService holdingService;

    @PostMapping
    public HoldingResponse createHolding(@Valid @RequestBody CreateHoldingRequest request) {
        return holdingService.createHolding(request);
    }

    @GetMapping
    public List<HoldingResponse> getHoldingByPortfolioId(@RequestParam Long portfolioId) {
        return holdingService.getHoldingsByPortfolioId(portfolioId);
    }
}
