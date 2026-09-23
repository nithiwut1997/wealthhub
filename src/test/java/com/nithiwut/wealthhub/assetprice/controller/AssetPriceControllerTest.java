package com.nithiwut.wealthhub.assetprice.controller;

import com.nithiwut.wealthhub.assetprice.dto.request.CreateAssetPriceRequest;
import com.nithiwut.wealthhub.assetprice.dto.response.AssetPriceResponse;
import com.nithiwut.wealthhub.assetprice.service.AssetPriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AssetPriceControllerTest {
    private AssetPriceService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AssetPriceService.class);
        mockMvc = standaloneSetup(new AssetPriceController(service)).build();
    }

    @Test
    void postsTimestampedPriceForAsset() throws Exception {
        LocalDateTime pricedAt = LocalDateTime.of(2026, 9, 23, 10, 0);
        CreateAssetPriceRequest request = new CreateAssetPriceRequest(new BigDecimal("520.50"), pricedAt);
        when(service.createAssetPrice(eq(7L), eq(request)))
            .thenReturn(new AssetPriceResponse(11L, 7L, new BigDecimal("520.50"), pricedAt));

        mockMvc.perform(post("/api/v1/assets/7/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"price": 520.50, "pricedAt": "2026-09-23T10:00:00"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(11))
            .andExpect(jsonPath("$.assetId").value(7))
            .andExpect(jsonPath("$.price").value(520.5))
            .andExpect(jsonPath("$.pricedAt").value("2026-09-23T10:00:00"));

        verify(service).createAssetPrice(7L, request);
    }

    @Test
    void rejectsNonPositivePriceAtApiBoundary() throws Exception {
        mockMvc.perform(post("/api/v1/assets/7/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"price": 0, "pricedAt": "2026-09-23T10:00:00"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
