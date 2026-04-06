package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) dạng record, đại diện cho một điểm dữ liệu trên biểu đồ xu hướng (trend chart).
 * Mỗi điểm tương ứng với một ngày và các chỉ số kinh doanh của ngày đó.
 *
 * @param date       Ngày của điểm dữ liệu.
 * @param revenue    Doanh thu của ngày hôm đó.
 * @param orderCount Số lượng đơn hàng của ngày hôm đó.
 */
public record StatisticsTrendPoint(
		LocalDate date,
		BigDecimal revenue,
		long orderCount
) {
}
