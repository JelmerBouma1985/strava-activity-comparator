package com.github.jelmerbouma1985.stravadatadashboard.services;

import com.github.jelmerbouma1985.stravadatadashboard.web.dto.ActivityData;
import com.github.jelmerbouma1985.stravadatadashboard.web.dto.StravaComparatorData;
import com.github.jelmerbouma1985.stravadatadashboard.web.dto.SummaryActivity;
import com.github.jelmerbouma1985.stravadatadashboard.web.mappers.StravaActivityMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StravaService {

    private final String stravaApiBaseUrl;
    private final RestTemplate restTemplate;

    public StravaService(@Value("${strava.api.base.url}") String stravaApiBaseUrl, RestTemplate restTemplate) {
        this.stravaApiBaseUrl = stravaApiBaseUrl;
        this.restTemplate = restTemplate;
    }

    public StravaComparatorData getActivitySummary(String accessToken, LocalDate startDate, LocalDate endDate) {

        ZoneId zoneId = ZoneId.systemDefault();
        long startEpoch = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli() / 1000;
        long endEpoch = endDate.plusDays(1L).atStartOfDay(zoneId).toInstant().toEpochMilli() / 1000;

        String url = stravaApiBaseUrl + "/athlete/activities?after=" + startEpoch + "&before=" + endEpoch + "&page=%d";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            int totalResponse = 30;
            int page = 1;
            List<SummaryActivity> activities = new ArrayList<>();
            while (totalResponse >= 30) {
                ResponseEntity<SummaryActivity[]> response = restTemplate.exchange(url.formatted(page), HttpMethod.GET, entity, SummaryActivity[].class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    var activityResponse = List.of(response.getBody());
                    activities.addAll(activityResponse);
                    totalResponse = activityResponse.size();
                    page++;
                } else {
                    throw new RuntimeException("Failed to fetch activities: " + response.getStatusCode());
                }

            }
            var activitiesByType = activities.stream()
                    .collect(Collectors.groupingBy(SummaryActivity::sportType, Collectors.mapping(s -> s, Collectors.toList())));

            var stravaActivities = activitiesByType.entrySet().stream()
                    .map(entry -> mapSummary(entry.getKey(), entry.getValue()))
                    .toList();

            var data = new StravaComparatorData();
            data.setStartDate(startDate);
            data.setEndDate(endDate);
            data.setTotalActivities(activities.size());
            data.setActivities(stravaActivities);

            return data;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Strava activities", e);
        }
    }

    private ActivityData mapSummary(String sportType, List<SummaryActivity> summaries) {
        return new ActivityData(sportType, summaries.size(), new StravaActivityMapper().apply(summaries));
    }
}
