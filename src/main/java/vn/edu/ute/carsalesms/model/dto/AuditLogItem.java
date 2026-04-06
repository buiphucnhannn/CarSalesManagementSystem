package vn.edu.ute.carsalesms.model.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, được sử dụng để hiển thị thông tin của một bản ghi nhật ký hệ thống (audit log) trên giao diện người dùng.
 * Dữ liệu này được tổng hợp từ thực thể `AuditLog` và các thực thể liên quan như `Staff`.
 *
 * @param id          Khóa chính của bản ghi nhật ký.
 * @param createdAt   Thời điểm hành động được ghi lại.
 * @param staffId     ID của nhân viên thực hiện hành động.
 * @param staffCode   Mã của nhân viên.
 * @param staffName   Tên của nhân viên.
 * @param staffRole   Vai trò của nhân viên tại thời điểm thực hiện hành động.
 * @param action      Tên của hành động (ví dụ: "CREATE", "UPDATE", "DELETE", "LOGIN").
 * @param entityName  Tên của loại đối tượng bị tác động (ví dụ: "Customer", "SaleOrder").
 * @param entityId    ID của đối tượng cụ thể bị tác động.
 * @param oldValue    Giá trị của đối tượng trước khi thay đổi (thường ở dạng chuỗi, có thể là JSON).
 * @param newValue    Giá trị của đối tượng sau khi thay đổi.
 */
public record AuditLogItem(
        Long id,
        LocalDateTime createdAt,
        Long staffId,
        String staffCode,
        String staffName,
        String staffRole,
        String action,
        String entityName,
        Long entityId,
        String oldValue,
        String newValue
) {
}
