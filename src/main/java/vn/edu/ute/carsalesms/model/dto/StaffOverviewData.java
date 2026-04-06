package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO (Data Transfer Object) dạng record, chứa các dữ liệu tổng quan được hiển thị trên bảng điều khiển (dashboard) của một nhân viên.
 *
 * @param pendingOrderCount   Số lượng đơn hàng do nhân viên này phụ trách đang ở trạng thái chờ xử lý.
 * @param todayRevenue        Tổng doanh thu mà nhân viên này mang lại trong ngày hôm nay.
 * @param todayTestDriveCount Số lượng lịch hẹn lái thử do nhân viên này phụ trách trong ngày hôm nay.
 * @param activeWarrantyCount Số lượng phiếu bảo hành đang có hiệu lực liên quan đến các đơn hàng của nhân viên này.
 * @param taskItems           Danh sách các công việc cần làm (ví dụ: đơn hàng cần xử lý, lịch hẹn sắp tới).
 */
public record StaffOverviewData(
        long pendingOrderCount,
        BigDecimal todayRevenue,
        long todayTestDriveCount,
        long activeWarrantyCount,
        List<DashboardTaskItem> taskItems
) {
    /**
     * Phương thức factory tĩnh để tạo một đối tượng `StaffOverviewData` rỗng.
     * Dùng làm giá trị mặc định để tránh lỗi khi dữ liệu chưa được tải.
     * @return Một đối tượng `StaffOverviewData` với các giá trị được khởi tạo là 0 hoặc danh sách rỗng.
     */
    public static StaffOverviewData empty() {
        return new StaffOverviewData(0L, BigDecimal.ZERO, 0L, 0L, List.of());
    }
}
