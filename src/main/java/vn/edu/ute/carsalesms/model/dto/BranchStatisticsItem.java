package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin thống kê của một chi nhánh.
 *
 * @param branchCode  Mã của chi nhánh.
 * @param branchName  Tên của chi nhánh.
 * @param totalOrders Tổng số lượng đơn hàng của chi nhánh trong khoảng thời gian thống kê.
 * @param revenue     Tổng doanh thu của chi nhánh trong khoảng thời gian thống kê.
 */
public record BranchStatisticsItem(
		String branchCode,
		String branchName,
		long totalOrders,
		BigDecimal revenue
) {
}
