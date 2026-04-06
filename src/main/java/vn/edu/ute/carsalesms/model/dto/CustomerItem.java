package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, chứa thông tin đầy đủ để hiển thị của một khách hàng trong bảng quản lý hoặc các giao diện chi tiết.
 * Đây là một đối tượng chỉ đọc (read-only), được tạo ra để truyền dữ liệu từ tầng service lên tầng view một cách an toàn và rõ ràng.
 * Nó không được sử dụng để gửi dữ liệu từ view về service để cập nhật (thay vào đó, `CustomerCommandRequest` sẽ được dùng).
 *
 * @param id             Khóa chính của khách hàng.
 * @param customerCode   Mã khách hàng (duy nhất).
 * @param fullName       Họ và tên đầy đủ.
 * @param phone          Số điện thoại.
 * @param email          Địa chỉ email.
 * @param gender         Giới tính (sử dụng enum `Gender`).
 * @param dateOfBirth    Ngày sinh.
 * @param identityNumber Số CMND/CCCD.
 * @param address        Địa chỉ liên hệ.
 * @param note           Ghi chú thêm về khách hàng.
 * @param createdAt      Thời điểm khách hàng được tạo trong hệ thống.
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
