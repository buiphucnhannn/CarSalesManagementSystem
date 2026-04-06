package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, được sử dụng để chứa thông tin hiển thị của một nhân viên trong các bảng hoặc danh sách.
 * DTO giúp tách biệt giữa cấu trúc dữ liệu của tầng database (Entity) và cấu trúc dữ liệu cần cho giao diện người dùng.
 * Việc sử dụng `record` của Java 14+ giúp tạo ra một lớp bất biến (immutable) và chỉ đọc (read-only) một cách ngắn gọn.
 *
 * @param id         Khóa chính của nhân viên.
 * @param staffCode  Mã nhân viên (duy nhất).
 * @param fullName   Họ và tên đầy đủ của nhân viên.
 * @param email      Địa chỉ email.
 * @param phone      Số điện thoại.
 * @param role       Vai trò của nhân viên trong hệ thống (ví dụ: ADMIN, STAFF).
 * @param branchId   ID của chi nhánh nơi nhân viên làm việc.
 * @param branchName Tên của chi nhánh.
 * @param status     Trạng thái của nhân viên (ví dụ: ACTIVE - đang làm việc, INACTIVE - đã nghỉ).
 * @param hasAccount Cờ boolean cho biết nhân viên này đã được cấp tài khoản đăng nhập hay chưa.
 * @param createdAt  Thời điểm nhân viên được thêm vào hệ thống.
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
