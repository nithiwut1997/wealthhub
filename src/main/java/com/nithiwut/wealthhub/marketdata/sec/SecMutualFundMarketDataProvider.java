package com.nithiwut.wealthhub.marketdata.sec;

import com.nithiwut.wealthhub.asset.entity.Asset;
import com.nithiwut.wealthhub.asset.entity.AssetType;
import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import com.nithiwut.wealthhub.common.exception.NotFoundException;
import com.nithiwut.wealthhub.marketdata.MarketDataProvider;
import com.nithiwut.wealthhub.marketdata.MarketPrice;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class SecMutualFundMarketDataProvider implements MarketDataProvider {
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

    private final SecMutualFundClient client;
    private final SecMarketDataProperties properties;
    private final Clock clock;

    public SecMutualFundMarketDataProvider(
        SecMutualFundClient client,
        SecMarketDataProperties properties
    ) {
        this.client = client;
        this.properties = properties;
        this.clock = Clock.system(BANGKOK);
    }

    @Override
    public AssetType supportedType() {
        return AssetType.MUTUAL_FUND;
    }

    @Override
    public MarketPrice getLatestPrice(Asset asset) {
        validateAsset(asset);
        int lookupDays = properties.getLookupDays();
        if (lookupDays < 1 || lookupDays > 31) {
            throw new BadRequestException(
                ErrorCode.MARKET_DATA_UNAVAILABLE, "SEC NAV lookup window is not configured correctly");
        }

        LocalDate today = LocalDate.now(clock);
        for (int daysBack = 0; daysBack < lookupDays; daysBack++) {
            List<SecDailyNavResponse> results = client.getDailyNav(
                asset.getExternalId(), today.minusDays(daysBack));
            if (!results.isEmpty()) {
                return mapPrice(results.getFirst(), asset);
            }
        }
        throw new NotFoundException(ErrorCode.ASSET_PRICE_NOT_FOUND,
            "No SEC NAV was available in the configured lookup window for asset " + asset.getSymbol());
    }

    private void validateAsset(Asset asset) {
        if (asset.getType() != AssetType.MUTUAL_FUND) {
            throw new BadRequestException("SEC NAV refresh only supports MUTUAL_FUND assets");
        }
        if (!StringUtils.hasText(asset.getExternalId())) {
            throw new BadRequestException("A mutual fund externalId is required for SEC NAV refresh");
        }
        if (!"THB".equalsIgnoreCase(asset.getCurrency())) {
            throw new BadRequestException(ErrorCode.UNSUPPORTED_CURRENCY,
                "SEC mutual fund NAV refresh only supports THB assets");
        }
    }

    private MarketPrice mapPrice(SecDailyNavResponse response, Asset asset) {
        if (response.nav() == null || response.nav().compareTo(BigDecimal.ZERO) <= 0) {
            throw malformed("SEC returned an invalid NAV", asset);
        }
        if (!StringUtils.hasText(response.navDate())) {
            throw malformed("SEC returned a NAV without a valuation date", asset);
        }
        try {
            // Parse only the documented date value; do not guess at alternate timestamp formats.
            LocalDate navDate = LocalDate.parse(response.navDate());
            return new MarketPrice(response.nav(), navDate.atStartOfDay());
        } catch (DateTimeParseException exception) {
            throw malformed("SEC returned an invalid NAV valuation date", asset);
        }
    }

    private BadRequestException malformed(String message, Asset asset) {
        return new BadRequestException(
            ErrorCode.MARKET_DATA_UNAVAILABLE, message + " for asset " + asset.getSymbol());
    }
}
