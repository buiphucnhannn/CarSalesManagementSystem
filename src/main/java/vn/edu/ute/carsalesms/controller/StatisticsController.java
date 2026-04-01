package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.StatisticsDashboardData;
import vn.edu.ute.carsalesms.service.impl.StatisticsServiceImpl;

import java.time.LocalDate;
import java.lang.reflect.Method;
import java.util.Objects;

public class StatisticsController {

    private final StatisticsServiceImpl statisticsService;

    public StatisticsController(StatisticsServiceImpl statisticsService) {
        this.statisticsService = Objects.requireNonNull(statisticsService, "statisticsService is required");
    }

    public StatisticsDashboardData getAdminStatistics(LocalDate fromDate, LocalDate toDate) {
        return invoke("getAdminStatistics", new Class<?>[]{LocalDate.class, LocalDate.class}, new Object[]{fromDate, toDate});
    }

    public StatisticsDashboardData getStaffStatistics(Long staffId, LocalDate fromDate, LocalDate toDate) {
        return invoke(
                "getStaffStatistics",
                new Class<?>[]{Long.class, LocalDate.class, LocalDate.class},
                new Object[]{staffId, fromDate, toDate}
        );
    }

    private StatisticsDashboardData invoke(String method, Class<?>[] paramTypes, Object[] args) {
        try {
            Method m = statisticsService.getClass().getMethod(method, paramTypes);
            Object result = m.invoke(statisticsService, args);
            return result instanceof StatisticsDashboardData data ? data : StatisticsDashboardData.empty();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tải dữ liệu thống kê.", ex);
        }
    }
}

