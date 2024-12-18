package com.github.jelmerbouma1985.stravadatadashboard.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SummaryActivity(
        @JsonProperty float distance, 
        @JsonProperty("elapsed_time") int elapsedTime, 
        @JsonProperty("total_elevation_gain") float totalElevationGain,
        @JsonProperty("sport_type") String sportType) {
}
