package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

public record StatisticsKpiItem(
        BigDecimal totalRevenue,
        long totalOrders,
        long paidOrders,
        BigDecimal averageOrderValue
) {
    public static StatisticsKpiItem empty() {
        return new StatisticsKpiItem(BigDecimal.ZERO, 0L, 0L, BigDecimal.ZERO);
    }
}

