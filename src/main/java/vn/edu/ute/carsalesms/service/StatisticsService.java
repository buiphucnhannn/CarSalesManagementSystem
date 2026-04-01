package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.StatisticsDashboardData;

import java.time.LocalDate;

public interface StatisticsService {

    StatisticsDashboardData getAdminStatistics(LocalDate fromDate, LocalDate toDate);

    StatisticsDashboardData getStaffStatistics(Long staffId, LocalDate fromDate, LocalDate toDate);
}

