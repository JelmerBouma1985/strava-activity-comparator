package com.github.jelmerbouma1985.stravadatadashboard.web.dto.activities;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StravaActivity {

    private long elapsedTime = 0L;
    private BigDecimal kilometers = BigDecimal.ZERO;
    private BigDecimal totalElevationGain = BigDecimal.ZERO;
}
