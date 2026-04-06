package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin thống kê của một mẫu xe trong danh sách "Top xe bán chạy".
 *
 * @param carCode      Mã của xe.
 * @param carName      Tên của xe.
 * @param soldQuantity Tổng số lượng đã bán được của mẫu xe này trong khoảng thời gian thống kê.
 * @param revenue      Tổng doanh thu mà mẫu xe này mang lại.
 */
public record TopCarStatisticsItem(
		String carCode,
		String carName,
		long soldQuantity,
		BigDecimal revenue
) {
}
