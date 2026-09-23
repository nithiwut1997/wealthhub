package com.nithiwut.wealthhub.holding.controller;

import com.nithiwut.wealthhub.holding.dto.response.HoldingResponse;
import com.nithiwut.wealthhub.holding.service.HoldingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class HoldingControllerTest {
    private HoldingService holdingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        holdingService = mock(HoldingService.class);
        mockMvc = standaloneSetup(new HoldingController(holdingService)).build();
    }

    @Test
    void listsCurrentHoldingsByPortfolio() throws Exception {
        when(holdingService.getHoldingsByPortfolioId(1L)).thenReturn(List.of(
            new HoldingResponse(
                3L,
                2L,
                "AAPL",
                "Apple",
                new BigDecimal("10.00000000"),
                new BigDecimal("100.00000000"),
                new BigDecimal("150.00000000"),
                new BigDecimal("1000.00000000"),
                new BigDecimal("1500.00000000"),
                new BigDecimal("500.00000000")
            )
        ));

        mockMvc.perform(get("/api/v1/holdings").param("portfolioId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(jsonPath("$[0].assetId").value(2))
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$[0].quantity").value(10.0))
            .andExpect(jsonPath("$[0].averageCost").value(100.0))
            .andExpect(jsonPath("$[0].latestPrice").value(150.0))
            .andExpect(jsonPath("$[0].marketValue").value(1500.0))
            .andExpect(jsonPath("$[0].unrealizedPnL").value(500.0));

        verify(holdingService).getHoldingsByPortfolioId(1L);
    }

    @Test
    void directHoldingCreationIsNotExposed() throws Exception {
        mockMvc.perform(post("/api/v1/holdings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "portfolioId": 1,
                      "assetId": 2,
                      "quantity": 10,
                      "averageCost": 100
                    }
                    """))
            .andExpect(status().isMethodNotAllowed());
    }
}
