package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Record chứa thông tin hiển thị của một khách hàng trong bảng quản lý.
 * Chỉ đọc (read-only) – không dùng để gửi lên service.
 *
 * @param id             khoá chính
 * @param customerCode   mã khách hàng (duy nhất)
 * @param fullName       họ tên đầy đủ
 * @param phone          số điện thoại
 * @param email          email
 * @param gender         giới tính (enum)
 * @param dateOfBirth    ngày sinh
 * @param identityNumber số CCCD/CMND
 * @param address        địa chỉ
 * @param note           ghi chú
 * @param createdAt      thời điểm tạo
 */
public record CustomerItem(
        Long id,
        String customerCode,
        String fullName,
        String phone,
        String email,
        Gender gender,
        LocalDate dateOfBirth,
        String identityNumber,
        String address,
        String note,
        LocalDateTime createdAt
) {
}
