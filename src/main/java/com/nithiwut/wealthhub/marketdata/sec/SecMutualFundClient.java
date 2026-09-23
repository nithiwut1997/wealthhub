package com.nithiwut.wealthhub.marketdata.sec;

import com.nithiwut.wealthhub.common.error.ErrorCode;
import com.nithiwut.wealthhub.common.exception.BadRequestException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.List;

@Component
class SecMutualFundClient {
    static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

    private final RestClient.Builder restClientBuilder;
    private final SecMarketDataProperties properties;

    SecMutualFundClient(RestClient.Builder restClientBuilder, SecMarketDataProperties properties) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
    }

    List<SecDailyNavResponse> getDailyNav(String projectId, LocalDate navDate) {
        validateConfiguration();
        try {
            List<SecDailyNavResponse> response = restClientBuilder.baseUrl(properties.getBaseUrl()).build()
                .get()
                .uri("/FundDailyInfo/{projId}/dailynav/{navDate}", projectId, navDate)
                .header(SUBSCRIPTION_KEY_HEADER, properties.getApiKey())
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND
                || exception.getStatusCode() == HttpStatus.NO_CONTENT) {
                return List.of();
            }
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getBaseUrl()) || !StringUtils.hasText(properties.getApiKey())) {
            throw new BadRequestException(
                ErrorCode.MARKET_DATA_UNAVAILABLE, "SEC market data is not configured");
        }
    }

    private BadRequestException unavailable() {
        return new BadRequestException(
            ErrorCode.MARKET_DATA_UNAVAILABLE, "SEC market data service is unavailable");
    }
}
