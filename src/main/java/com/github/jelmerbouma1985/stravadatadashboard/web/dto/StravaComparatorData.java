package com.github.jelmerbouma1985.stravadatadashboard.web.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class StravaComparatorData {

    private LocalDate startDate;
    private LocalDate endDate;
    private int totalActivities;
    private List<ActivityData> activities = new ArrayList<>();
}
