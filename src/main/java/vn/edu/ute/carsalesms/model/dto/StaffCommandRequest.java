package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, chứa dữ liệu cho lệnh tạo mới hoặc cập nhật thông tin một nhân viên.
 *
 * @param id         ID của nhân viên. Nếu là `null`, đây là yêu cầu tạo mới. Nếu có giá trị, đây là yêu cầu cập nhật.
 * @param staffCode  Mã nhân viên (bắt buộc, duy nhất).
 * @param fullName   Họ và tên đầy đủ (bắt buộc).
 * @param email      Địa chỉ email (tùy chọn, nhưng phải là duy nhất nếu có).
 * @param phone      Số điện thoại (tùy chọn).
 * @param role       Vai trò của nhân viên trong hệ thống (ADMIN / STAFF, bắt buộc).
 * @param branchId   ID của chi nhánh nơi nhân viên làm việc (bắt buộc).
 * @param status     Trạng thái của nhân viên (ACTIVE / INACTIVE).
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
