package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * Record mang dữ liệu khi thêm mới hoặc cập nhật nhân viên.
 *
 * @param id         null nếu thêm mới, non-null nếu sửa
 * @param staffCode  mã nhân viên (bắt buộc, duy nhất)
 * @param fullName   họ tên đầy đủ (bắt buộc)
 * @param email      email (tùy chọn, unique)
 * @param phone      số điện thoại (tùy chọn)
 * @param role       vai trò ADMIN / STAFF (bắt buộc)
 * @param branchId   id chi nhánh (bắt buộc)
 * @param status     trạng thái ACTIVE / INACTIVE
 */
public record StaffCommandRequest(
        Long id,
        String staffCode,
        String fullName,
        String email,
        String phone,
        StaffRole role,
        Long branchId,
        Status status
) {
}
