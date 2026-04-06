package vn.edu.ute.carsalesms.model.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, đại diện cho một mục công việc (task) cần thực hiện,
 * được hiển thị trên bảng điều khiển (dashboard) của nhân viên.
 * Ví dụ: một đơn hàng đang chờ xử lý, một lịch lái thử sắp diễn ra.
 *
 * @param action        Mô tả về công việc cần làm (ví dụ: "Đơn hàng mới", "Lái thử xe Vinfast VF8").
 * @param customerName  Tên của khách hàng liên quan đến công việc.
 * @param dueAt         Thời điểm đến hạn hoặc thời gian diễn ra công việc.
 * @param status        Trạng thái hiện tại của công việc (ví dụ: "PENDING", "SCHEDULED").
 */
public record DashboardTaskItem(
        String action,
        String customerName,
        LocalDateTime dueAt,
        String status
) {
}
