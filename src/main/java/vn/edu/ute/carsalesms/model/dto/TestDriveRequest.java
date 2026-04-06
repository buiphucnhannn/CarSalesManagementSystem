package vn.edu.ute.carsalesms.model.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, chứa thông tin cần thiết để tạo một lịch hẹn lái thử mới.
 * Đối tượng này được gửi từ tầng view lên tầng service để xử lý.
 *
 * @param customerId    ID của khách hàng đăng ký lái thử.
 * @param carId         ID của chiếc xe được đăng ký lái thử.
 * @param staffId       ID của nhân viên phụ trách lịch hẹn này.
 * @param scheduledTime Thời gian dự kiến diễn ra buổi lái thử.
 * @param note          Ghi chú cho lịch hẹn.
 */
public record TestDriveRequest(
        Long customerId,
        Long carId,
        Long staffId,
        LocalDateTime scheduledTime,
        String note
) {
}
