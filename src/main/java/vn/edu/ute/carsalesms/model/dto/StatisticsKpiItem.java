package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) dạng record, chứa các chỉ số hiệu suất kinh doanh chính (KPI - Key Performance Indicators).
 * Dùng để hiển thị các con số tổng hợp quan trọng trên màn hình thống kê.
 *
 * @param totalRevenue        Tổng doanh thu trong khoảng thời gian được chọn.
 * @param totalOrders         Tổng số lượng đơn hàng đã tạo.
 * @param paidOrders          Tổng số lượng đơn hàng đã được thanh toán đầy đủ.
 * @param averageOrderValue   Giá trị trung bình của một đơn hàng (Tổng doanh thu / Số đơn hàng đã thanh toán).
 */
public record StatisticsKpiItem(
        BigDecimal totalRevenue,
        long totalOrders,
        long paidOrders,
        BigDecimal averageOrderValue
) {
    /**
     * Phương thức factory tĩnh để tạo một đối tượng `StatisticsKpiItem` rỗng.
     * @return Một đối tượng `StatisticsKpiItem` với tất cả các giá trị là 0.
     */
    public static StatisticsKpiItem empty() {
        return new StatisticsKpiItem(BigDecimal.ZERO, 0L, 0L, BigDecimal.ZERO);
    }
}
