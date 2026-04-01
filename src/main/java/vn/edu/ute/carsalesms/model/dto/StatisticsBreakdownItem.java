package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

public record StatisticsBreakdownItem(
		String label,
		long count,
		BigDecimal amount
) {
}

