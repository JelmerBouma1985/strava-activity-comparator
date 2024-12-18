package com.github.jelmerbouma1985.stravadatadashboard.web.views;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.bar.DataLabels;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.github.jelmerbouma1985.stravadatadashboard.services.StravaService;
import com.github.jelmerbouma1985.stravadatadashboard.web.dto.ActivityData;
import com.github.jelmerbouma1985.stravadatadashboard.web.dto.StravaComparatorData;
import com.github.jelmerbouma1985.stravadatadashboard.web.dto.activities.StravaActivity;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("strava/comparator")
public class CompareView extends VerticalLayout implements BeforeEnterObserver {
    private String token;

    public CompareView(StravaService stravaService) {
        var compareButton = new Button("Compare");
        compareButton.setEnabled(false);

        var startDatePicker1 = createDatePicker("Start date");
        var startDatePicker2 = createDatePicker("Start date");
        var endDatePicker1 = createDatePicker("End date");
        endDatePicker1.setEnabled(false);
        var endDatePicker2 = createDatePicker("End date");
        endDatePicker2.setEnabled(false);
        startDatePicker1.addValueChangeListener((listener) -> {
            endDatePicker1.setEnabled(true);
            endDatePicker1.setInitialPosition(startDatePicker1.getValue());
            endDatePicker1.setMin(startDatePicker1.getValue());
            compareButton.setEnabled(enableButton(startDatePicker1, endDatePicker1, startDatePicker2, endDatePicker2));
        });

        startDatePicker2.addValueChangeListener((listener) -> {
            endDatePicker2.setEnabled(true);
            endDatePicker2.setInitialPosition(startDatePicker2.getValue());
            endDatePicker2.setMin(startDatePicker2.getValue());
            compareButton.setEnabled(enableButton(startDatePicker1, endDatePicker1, startDatePicker2, endDatePicker2));
        });

        endDatePicker1.addValueChangeListener((listener) -> compareButton.setEnabled(enableButton(startDatePicker1, endDatePicker1, startDatePicker2, endDatePicker2)));
        endDatePicker2.addValueChangeListener((listener) -> compareButton.setEnabled(enableButton(startDatePicker1, endDatePicker1, startDatePicker2, endDatePicker2)));

        HorizontalLayout fromToDates1 = new HorizontalLayout(startDatePicker1, endDatePicker1);
        HorizontalLayout fromToDates2 = new HorizontalLayout(startDatePicker2, endDatePicker2);
        add(fromToDates1, fromToDates2, compareButton);

        compareButton.addClickListener((event -> {
            var dataFirstDates = stravaService.getActivitySummary(token, startDatePicker1.getValue(), endDatePicker1.getValue());
            var dataSecondDates = stravaService.getActivitySummary(token, startDatePicker2.getValue(), endDatePicker2.getValue());
            remove(getChildren().filter(c -> c.getClass().getSimpleName().equals(ApexCharts.class.getSimpleName())).toList());

            addTotalActivitiesChart(dataFirstDates, dataSecondDates);
            addElapsedTimeChart(dataFirstDates, dataSecondDates);
            addTotalKilometersChart(dataFirstDates, dataSecondDates);
            addTotalElevationChart(dataFirstDates, dataSecondDates);
        }));
    }

    private void addTotalActivitiesChart(StravaComparatorData dataFirstDates,
                                         StravaComparatorData dataSecondDates) {

        List<String> chartColumns = new ArrayList<>();
        chartColumns.addFirst("Total");
        Stream.concat(dataFirstDates.getActivities().stream(), dataSecondDates.getActivities().stream())
                .map(ActivityData::getSportType)
                .distinct()
                .collect(Collectors.toCollection(() -> chartColumns));

        List<Object> dataRange1 = new ArrayList<>();
        dataRange1.add(dataFirstDates.getTotalActivities());
        List<Object> dataRange2 = new ArrayList<>();
        dataRange2.add(dataSecondDates.getTotalActivities());

        for (String activity : chartColumns) {
            if (!activity.equals("Total")) {
                dataRange1.add(dataFirstDates.getActivities().stream().filter(a -> a.getSportType().equals(activity)).map(ActivityData::getTotalActivities).findFirst().orElse(0));
                dataRange2.add(dataSecondDates.getActivities().stream().filter(a -> a.getSportType().equals(activity)).map(ActivityData::getTotalActivities).findFirst().orElse(0));
            }
        }

        add(createChart("Total activities", chartColumns, dataFirstDates, dataSecondDates, dataRange1.toArray(), dataRange2.toArray()));
    }

    private void addElapsedTimeChart(StravaComparatorData dataFirstDates,
                                     StravaComparatorData dataSecondDates) {

        List<String> chartColumns = new ArrayList<>();
        chartColumns.addFirst("Total");
        Stream.concat(dataFirstDates.getActivities().stream(), dataSecondDates.getActivities().stream())
                .map(ActivityData::getSportType)
                .distinct()
                .collect(Collectors.toCollection(() -> chartColumns));

        List<Object> dataRange1 = new ArrayList<>();
        dataRange1.add(minutes(dataFirstDates.getActivities().stream().map(ActivityData::getActivityData).mapToLong(StravaActivity::getElapsedTime).sum()));
        List<Object> dataRange2 = new ArrayList<>();
        dataRange2.add(minutes(dataSecondDates.getActivities().stream().map(ActivityData::getActivityData).mapToLong(StravaActivity::getElapsedTime).sum()));

        for (String activity : List.copyOf(chartColumns)) {
            if (!activity.equals("Total")) {
                long totalElapsedTimeRange1 = dataFirstDates.getActivities().stream()
                        .filter(a -> a.getSportType().equals(activity))
                        .map(ActivityData::getActivityData)
                        .map(StravaActivity::getElapsedTime)
                        .findFirst()
                        .orElse(0L);
                long totalElapsedTimeRange2 = dataSecondDates.getActivities().stream()
                        .filter(a -> a.getSportType().equals(activity))
                        .map(ActivityData::getActivityData)
                        .map(StravaActivity::getElapsedTime)
                        .findFirst()
                        .orElse(0L);

                if(totalElapsedTimeRange1 > 0L || totalElapsedTimeRange2 > 0L) {
                    dataRange1.add(minutes(totalElapsedTimeRange1));
                    dataRange2.add(minutes(totalElapsedTimeRange2));
                } else {
                    chartColumns.remove(activity);
                }
            }
        }

        add(createChart("Total elapsed time in minutes", chartColumns, dataFirstDates, dataSecondDates, dataRange1.toArray(), dataRange2.toArray()));
    }

    private void addTotalKilometersChart(StravaComparatorData dataFirstDates,
                                     StravaComparatorData dataSecondDates) {
        List<String> chartColumns = new ArrayList<>();
        chartColumns.addFirst("Total");
        Stream.concat(dataFirstDates.getActivities().stream(), dataSecondDates.getActivities().stream())
                .map(ActivityData::getSportType)
                .distinct()
                .collect(Collectors.toCollection(() -> chartColumns));

        List<Object> dataRange1 = new ArrayList<>();
        dataRange1.add(dataFirstDates.getActivities().stream().map(ActivityData::getActivityData).map(StravaActivity::getKilometers).reduce(BigDecimal.ZERO, BigDecimal::add));
        List<Object> dataRange2 = new ArrayList<>();
        dataRange2.add(dataSecondDates.getActivities().stream().map(ActivityData::getActivityData).map(StravaActivity::getKilometers).reduce(BigDecimal.ZERO, BigDecimal::add));

        for (String activity : List.copyOf(chartColumns)) {
            if (!activity.equals("Total")) {
                var totalKmRange1 = dataFirstDates.getActivities().stream().filter(a -> a.getSportType().equals(activity)).map(ActivityData::getActivityData).map(StravaActivity::getKilometers).findFirst().orElse(
                        BigDecimal.ZERO);
                var totalKmRange2 = dataSecondDates.getActivities().stream().filter(a -> a.getSportType().equals(activity)).map(ActivityData::getActivityData).map(StravaActivity::getKilometers).findFirst().orElse(
                        BigDecimal.ZERO);

                if(totalKmRange1.signum() > 0 || totalKmRange2.signum() > 0) {
                    dataRange1.add(totalKmRange1);
                    dataRange2.add(totalKmRange2);
                } else {
                    chartColumns.remove(activity);
                }
            }
        }

        add(createChart("Total kilometers", chartColumns, dataFirstDates, dataSecondDates, dataRange1.toArray(), dataRange2.toArray()));
    }

    private void addTotalElevationChart(StravaComparatorData dataFirstDates,
                                        StravaComparatorData dataSecondDates) {

        List<String> chartColumns = new ArrayList<>();
        chartColumns.addFirst("Total");
        Stream.concat(dataFirstDates.getActivities().stream(), dataSecondDates.getActivities().stream())
                .map(ActivityData::getSportType)
                .distinct()
                .collect(Collectors.toCollection(() -> chartColumns));

        List<Object> dataRange1 = new ArrayList<>();
        dataRange1.add(dataFirstDates.getActivities().stream().map(ActivityData::getActivityData).map(StravaActivity::getTotalElevationGain).reduce(BigDecimal.ZERO, BigDecimal::add));
        List<Object> dataRange2 = new ArrayList<>();
        dataRange2.add(dataSecondDates.getActivities().stream().map(ActivityData::getActivityData).map(StravaActivity::getTotalElevationGain).reduce(BigDecimal.ZERO, BigDecimal::add));

        for (String activity : List.copyOf(chartColumns)) {
            if (!activity.equals("Total")) {
                var totalElevationRange1 = dataFirstDates.getActivities().stream().filter(a -> a.getSportType().equals(activity)).map(ActivityData::getActivityData).map(StravaActivity::getTotalElevationGain).findFirst().orElse(
                        BigDecimal.ZERO);
                var totalElevationRange2 = dataSecondDates.getActivities().stream().filter(a -> a.getSportType().equals(activity)).map(ActivityData::getActivityData).map(StravaActivity::getTotalElevationGain).findFirst().orElse(
                        BigDecimal.ZERO);

                if(totalElevationRange1.signum() > 0 || totalElevationRange2.signum() > 0) {
                    dataRange1.add(totalElevationRange1);
                    dataRange2.add(totalElevationRange2);
                } else {
                    chartColumns.remove(activity);
                }
            }
        }

        add(createChart("Total elevation gain", chartColumns, dataFirstDates, dataSecondDates, dataRange1.toArray(), dataRange2.toArray()));
    }

    private boolean enableButton(DatePicker startDatePicker1, DatePicker endDatePicker1, DatePicker startDatePicker2, DatePicker endDatePicker2) {
        return !startDatePicker1.isEmpty()
                && !endDatePicker1.isEmpty()
                && !startDatePicker2.isEmpty()
                && !endDatePicker2.isEmpty()
                && !endDatePicker1.getValue().isBefore(startDatePicker1.getValue())
                && !endDatePicker2.getValue().isBefore(startDatePicker2.getValue());
    }

    private ApexCharts createChart(String title,
                                   List<String> chartNames,
                                   StravaComparatorData dataFirstDates,
                                   StravaComparatorData dataSecondDates,
                                   Object[] dataSerie1,
                                   Object[] dataSerie2) {
        ApexCharts chart = ApexChartsBuilder.get()
                .withTitle(TitleSubtitleBuilder.get().withText(title).build())
                .withChart(ChartBuilder.get().withType(Type.BAR).build())
                .withSeries(
                        new Series<>("%s-%s".formatted(dataFirstDates.getStartDate(),
                                                       dataFirstDates.getEndDate()),
                                     dataSerie1),
                        new Series<>("%s-%s".formatted(dataSecondDates.getStartDate(),
                                                       dataSecondDates.getEndDate()),
                                     dataSerie2)
                )
                .withPlotOptions(PlotOptionsBuilder.get()
                                         .withBar(BarBuilder.get().withHorizontal(false).withDataLabels(new DataLabels("top")).build())
                                         .build())
                .withDataLabels(DataLabelsBuilder.get()
                                        .withEnabled(true)
                                        .build())
                .withStroke(StrokeBuilder.get().withShow(true).build())
                .withTooltip(TooltipBuilder.get().withShared(true).withIntersect(false).build())
                .withXaxis(XAxisBuilder.get().withCategories(chartNames).build())
                .build();
        chart.setHeight("200px");
        return chart;
    }

    private long minutes(long seconds) {
        return TimeUnit.SECONDS.toMinutes(seconds);
    }

    private DatePicker createDatePicker(String label) {
        DatePicker.DatePickerI18n singleFormatI18n = new DatePicker.DatePickerI18n();
        singleFormatI18n.setDateFormat("yyyy-MM-dd");

        var datePicker = new DatePicker(label);
        datePicker.setI18n(singleFormatI18n);
        datePicker.setRequired(true);
        return datePicker;
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        token = queryParameters.getSingleParameter("token").orElseThrow(() -> new IllegalStateException("Er is geen accesstoken aanwezig. Data kan niet worden opgevraagd in " +
                                                                                                                "Strava"));
    }
}
