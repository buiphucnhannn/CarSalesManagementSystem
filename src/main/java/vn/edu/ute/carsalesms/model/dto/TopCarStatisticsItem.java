package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

public record TopCarStatisticsItem(
		String carCode,
		String carName,
		long soldQuantity,
		BigDecimal revenue
) {
}

