package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin của một lịch hẹn lái thử xe.
 * Dữ liệu được tổng hợp từ các thực thể TestDrive, Customer, Car, và Staff.
 *
 * @param id              Khóa chính của lịch hẹn.
 * @param testDriveCode   Mã của lịch hẹn lái thử.
 * @param customerName    Tên của khách hàng đăng ký lái thử.
 * @param carModel        Tên/mẫu của chiếc xe được lái thử.
 * @param staffName       Tên của nhân viên phụ trách lịch hẹn.
 * @param scheduledTime   Thời gian dự kiến diễn ra buổi lái thử.
 * @param result          Kết quả hoặc phản hồi từ khách hàng sau buổi lái thử.
 * @param status          Trạng thái của lịch hẹn (ví dụ: SCHEDULED, COMPLETED, CANCELLED).
 * @param note            Ghi chú cho lịch hẹn.
 */
public record TestDriveItem(
        Long id,
        String testDriveCode,
        String customerName,
        String carModel,
        String staffName,
        LocalDateTime scheduledTime,
        String result,
        TestDriveStatus status,
        String note
) {
}
