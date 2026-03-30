package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;

/**
 * Record chứa thông tin hiển thị của một nhân viên trong bảng quản lý.
 * Chỉ đọc (read-only).
 *
 * @param id         khoá chính
 * @param staffCode  mã nhân viên (duy nhất)
 * @param fullName   họ tên đầy đủ
 * @param email      email
 * @param phone      số điện thoại
 * @param role       vai trò ADMIN / STAFF
 * @param branchId   id chi nhánh
 * @param branchName tên chi nhánh
 * @param status     trạng thái ACTIVE / INACTIVE
 * @param hasAccount nhân viên đã có tài khoản chưa
 * @param createdAt  thời điểm tạo
 */
public record StaffItem(
        Long id,
        String staffCode,
        String fullName,
        String email,
        String phone,
        StaffRole role,
        Long branchId,
        String branchName,
        Status status,
        boolean hasAccount,
        LocalDateTime createdAt
) {
}
