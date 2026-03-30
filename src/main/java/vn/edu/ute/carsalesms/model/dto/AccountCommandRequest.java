package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * Record mang dữ liệu khi tạo mới hoặc cập nhật tài khoản đăng nhập.
 *
 * @param id           null nếu tạo mới, non-null nếu cập nhật
 * @param staffId      id nhân viên cần gắn tài khoản (bắt buộc)
 * @param username     tên đăng nhập (bắt buộc, duy nhất)
 * @param rawPassword  mật khẩu thô sẽ được hash ở service (bắt buộc khi tạo, null = giữ nguyên khi sửa)
 * @param status       ACTIVE / INACTIVE
 */
public record AccountCommandRequest(
        Long id,
        Long staffId,
        String username,
        String rawPassword,
        Status status
) {
}
