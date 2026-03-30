package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;

/**
 * Record chứa thông tin hiển thị của một tài khoản đăng nhập.
 * Gắn với nhân viên (1-1).
 *
 * @param id                   khoá chính tài khoản
 * @param staffId              id nhân viên tương ứng
 * @param staffCode            mã nhân viên
 * @param staffFullName        tên nhân viên
 * @param username             tên đăng nhập
 * @param status               ACTIVE / INACTIVE
 * @param locked               tài khoản bị khóa hay không
 * @param failedLoginAttempts  số lần đăng nhập thất bại
 * @param lastLoginAt          thời điểm đăng nhập gần nhất
 * @param createdAt            thời điểm tạo tài khoản
 */
public record AccountItem(
        Long id,
        Long staffId,
        String staffCode,
        String staffFullName,
        String username,
        Status status,
        boolean locked,
        int failedLoginAttempts,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
