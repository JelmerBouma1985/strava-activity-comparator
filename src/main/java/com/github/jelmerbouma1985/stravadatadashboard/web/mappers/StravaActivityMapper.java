package com.github.jelmerbouma1985.stravadatadashboard.web.mappers;

import com.github.jelmerbouma1985.stravadatadashboard.web.dto.SummaryActivity;
import com.github.jelmerbouma1985.stravadatadashboard.web.dto.activities.StravaActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

public class StravaActivityMapper implements Function<List<SummaryActivity>, StravaActivity> {

    @Override
    public StravaActivity apply(List<SummaryActivity> summaryActivities) {

        var totalDistance = BigDecimal.valueOf(summaryActivities.stream()
                                                       .mapToDouble(SummaryActivity::distance).sum())
                .divide(new BigDecimal("1000").setScale(2, RoundingMode.HALF_UP), RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);

        var totalElevation = BigDecimal.valueOf(summaryActivities.stream()
                                                        .mapToDouble(SummaryActivity::totalElevationGain).sum())
                .setScale(2, RoundingMode.HALF_UP);

        var elapsedTime = summaryActivities.stream().mapToLong(SummaryActivity::elapsedTime).sum();

        StravaActivity activity = new StravaActivity();
        activity.setKilometers(totalDistance);
        activity.setTotalElevationGain(totalElevation);
        activity.setElapsedTime(elapsedTime);
        return activity;
    }


}
