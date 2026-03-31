package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BranchSalesReportItem(
        Long branchId,
        String branchCode,
        String branchName,
        Status branchStatus,
        long totalOrders,
        long paidOrders,
        long pendingOrders,
        long cancelledOrders,
        BigDecimal revenue,
        LocalDateTime latestOrderAt
) {
}

