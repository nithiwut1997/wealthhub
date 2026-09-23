package com.nithiwut.wealthhub.transaction.controller;

import com.nithiwut.wealthhub.transaction.dto.request.CreateTransactionRequest;
import com.nithiwut.wealthhub.transaction.dto.response.TransactionResponse;
import com.nithiwut.wealthhub.transaction.service.InvestmentTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class InvestmentTransactionController {
    private final InvestmentTransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    @GetMapping
    public List<TransactionResponse> getTransactionHistory(@RequestParam Long portfolioId) {
        return transactionService.getTransactionHistory(portfolioId);
    }
}
