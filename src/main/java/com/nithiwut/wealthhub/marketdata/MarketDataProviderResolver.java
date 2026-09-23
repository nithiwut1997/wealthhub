package com.nithiwut.wealthhub.marketdata;

import com.nithiwut.wealthhub.asset.entity.AssetType;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarketDataProviderResolver {
    private final List<MarketDataProvider> providers;

    public MarketDataProviderResolver(List<MarketDataProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public MarketDataProvider resolve(AssetType type) {
        return providers.stream()
            .filter(provider -> provider.supportedType() == type)
            .findFirst()
            .orElseThrow(() -> new BadRequestException("No market data provider supports asset type " + type));
    }
}
