package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminOverviewData(
        BigDecimal monthlyRevenue,
        long todayOrderCount,
        long pendingOrderCount,
        long todayTestDriveCount,
        List<AdminRecentOrderItem> recentOrders
) {
    public static AdminOverviewData empty() {
        return new AdminOverviewData(BigDecimal.ZERO, 0L, 0L, 0L, List.of());
    }
}

