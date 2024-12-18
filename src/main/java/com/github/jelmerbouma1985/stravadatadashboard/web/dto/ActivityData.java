package com.github.jelmerbouma1985.stravadatadashboard.web.dto;

import com.github.jelmerbouma1985.stravadatadashboard.web.dto.activities.StravaActivity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivityData {

    private String sportType;
    private int totalActivities;
    private StravaActivity activityData;
}
