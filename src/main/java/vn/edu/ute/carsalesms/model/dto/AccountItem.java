package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, chứa thông tin hiển thị của một tài khoản đăng nhập.
 * Dữ liệu này được tổng hợp từ cả hai thực thể `Account` và `Staff` để tiện cho việc hiển thị trên giao diện quản lý tài khoản.
 *
 * @param id                   Khóa chính của tài khoản.
 * @param staffId              ID của nhân viên sở hữu tài khoản này.
 * @param staffCode            Mã của nhân viên.
 * @param staffFullName        Tên đầy đủ của nhân viên.
 * @param username             Tên đăng nhập của tài khoản.
 * @param status               Trạng thái của tài khoản (ACTIVE / INACTIVE).
 * @param locked               Cờ boolean cho biết tài khoản có đang bị khóa hay không (ví dụ: do đăng nhập sai nhiều lần).
 * @param failedLoginAttempts  Số lần đăng nhập thất bại liên tiếp.
 * @param lastLoginAt          Thời điểm đăng nhập thành công gần nhất.
 * @param createdAt            Thời điểm tài khoản được tạo.
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
