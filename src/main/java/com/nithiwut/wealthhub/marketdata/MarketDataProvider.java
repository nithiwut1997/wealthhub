package com.nithiwut.wealthhub.marketdata;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.entity.AssetType;

public interface MarketDataProvider {
    AssetType supportedType();

    MarketPrice getLatestPrice(Asset asset);
}
