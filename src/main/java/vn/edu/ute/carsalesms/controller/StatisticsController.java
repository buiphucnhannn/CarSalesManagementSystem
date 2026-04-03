package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.StatisticsDashboardData;
import vn.edu.ute.carsalesms.service.StatisticsService;

import java.time.LocalDate;
import java.util.Objects;

public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = Objects.requireNonNull(statisticsService, "statisticsService is required");
    }

    public StatisticsDashboardData getAdminStatistics(LocalDate fromDate, LocalDate toDate) {
        return statisticsService.getAdminStatistics(fromDate, toDate);
    }

    public StatisticsDashboardData getStaffStatistics(Long staffId, LocalDate fromDate, LocalDate toDate) {
        return statisticsService.getStaffStatistics(staffId, fromDate, toDate);
    }
}

