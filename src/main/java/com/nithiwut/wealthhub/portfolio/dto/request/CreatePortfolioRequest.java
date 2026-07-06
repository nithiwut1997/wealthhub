package com.nithiwut.wealthhub.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePortfolioRequest(
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100)
    String name
) {
}
