package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, dùng để chứa thông tin hiển thị của một chi nhánh.
 * Đây là một đối tượng chỉ đọc, được sử dụng để truyền dữ liệu từ tầng service lên tầng view
 * mà không làm lộ cấu trúc của entity.
 *
 * @param id          Khóa chính của chi nhánh.
 * @param branchCode  Mã chi nhánh (duy nhất).
 * @param branchName  Tên đầy đủ của chi nhánh.
 * @param address     Địa chỉ của chi nhánh.
 * @param phone       Số điện thoại liên hệ.
 * @param email       Địa chỉ email liên hệ.
 * @param status      Trạng thái hoạt động của chi nhánh (ACTIVE hoặc INACTIVE).
 * @param createdAt   Thời điểm chi nhánh được thêm vào hệ thống.
 */
public record BranchItem(
        Long id,
        String branchCode,
        String branchName,
        String address,
        String phone,
        String email,
        Status status,
        LocalDateTime createdAt
) {
}
