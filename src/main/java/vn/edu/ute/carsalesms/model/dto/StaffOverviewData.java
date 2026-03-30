package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record StaffOverviewData(
        long pendingOrderCount,
        BigDecimal todayRevenue,
        long todayTestDriveCount,
        long activeWarrantyCount,
        List<DashboardTaskItem> taskItems
) {
    public static StaffOverviewData empty() {
        return new StaffOverviewData(0L, BigDecimal.ZERO, 0L, 0L, List.of());
    }
}

