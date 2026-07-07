package com.nithiwut.wealthhub.asset.controller;

import com.nithiwut.wealthhub.asset.dto.request.CreateAssetRequest;
import com.nithiwut.wealthhub.asset.dto.response.AssetResponse;
import com.nithiwut.wealthhub.asset.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @PostMapping
    public AssetResponse createAsset(@Valid @RequestBody CreateAssetRequest request) {
        return assetService.createAsset(request);
    }

    @GetMapping
    public List<AssetResponse> getAllAssets() {
        return assetService.getAllAssets();
    }

    @GetMapping("/{id}")
    public AssetResponse getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id);
    }
}
