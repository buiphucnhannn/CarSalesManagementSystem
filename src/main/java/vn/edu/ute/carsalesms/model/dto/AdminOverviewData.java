package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO (Data Transfer Object) dạng record, chứa các dữ liệu tổng quan được hiển thị trên bảng điều khiển (dashboard) của quản trị viên.
 *
 * @param monthlyRevenue      Tổng doanh thu trong tháng hiện tại.
 * @param todayOrderCount     Tổng số lượng đơn hàng được tạo trong ngày hôm nay.
 * @param pendingOrderCount   Tổng số lượng đơn hàng đang ở trạng thái chờ xử lý.
 * @param todayTestDriveCount Tổng số lượng lịch hẹn lái thử được đặt trong ngày hôm nay.
 * @param recentOrders        Danh sách các đơn hàng được tạo gần đây nhất.
 */
public record AdminOverviewData(
        BigDecimal monthlyRevenue,
        long todayOrderCount,
        long pendingOrderCount,
        long todayTestDriveCount,
        List<AdminRecentOrderItem> recentOrders
) {
    /**
     * Phương thức factory tĩnh để tạo một đối tượng `AdminOverviewData` rỗng.
     * Hữu ích khi cần một giá trị mặc định trước khi dữ liệu thực được tải.
     * @return Một đối tượng `AdminOverviewData` với các giá trị được khởi tạo là 0 hoặc danh sách rỗng.
     */
    public static AdminOverviewData empty() {
        return new AdminOverviewData(BigDecimal.ZERO, 0L, 0L, 0L, List.of());
    }
}
