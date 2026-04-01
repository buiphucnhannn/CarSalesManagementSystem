package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StatisticsTrendPoint(
		LocalDate date,
		BigDecimal revenue,
		long orderCount
) {
}

