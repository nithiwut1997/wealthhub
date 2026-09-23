package com.nithiwut.wealthhub.marketdata.sec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
record SecDailyNavResponse(
    @JsonProperty("proj_id") String projectId,
    @JsonProperty("nav_date") String navDate,
    @JsonProperty("last_val") BigDecimal nav
) {
}
