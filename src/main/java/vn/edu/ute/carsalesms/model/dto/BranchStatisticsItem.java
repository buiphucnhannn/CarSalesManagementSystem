package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

public record BranchStatisticsItem(
		String branchCode,
		String branchName,
		long totalOrders,
		BigDecimal revenue
) {
}

