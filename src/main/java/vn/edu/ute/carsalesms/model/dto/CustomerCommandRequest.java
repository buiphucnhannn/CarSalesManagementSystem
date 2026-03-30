package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Gender;

import java.time.LocalDate;

/**
 * Record mang dữ liệu khi thêm mới hoặc cập nhật khách hàng.
 * Được tạo từ dialog nhập liệu và chuyển lên service để xử lý.
 *
 * @param id             null nếu thêm mới, non-null nếu sửa
 * @param customerCode   mã khách hàng (bắt buộc, duy nhất)
 * @param fullName       họ tên đầy đủ (bắt buộc)
 * @param phone          số điện thoại (bắt buộc)
 * @param email          email (tùy chọn)
 * @param gender         giới tính (tùy chọn)
 * @param dateOfBirth    ngày sinh (tùy chọn)
 * @param identityNumber số CCCD/CMND (tùy chọn)
 * @param address        địa chỉ (tùy chọn)
 * @param note           ghi chú (tùy chọn)
 */
public record CustomerCommandRequest(
        Long id,
        String customerCode,
        String fullName,
        String phone,
        String email,
        Gender gender,
        LocalDate dateOfBirth,
        String identityNumber,
        String address,
        String note
) {
}
