package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin của một thương hiệu xe trong giao diện quản lý.
 *
 * @param id        Khóa chính của thương hiệu.
 * @param brandCode Mã thương hiệu.
 * @param brandName Tên thương hiệu.
 * @param country   Quốc gia của thương hiệu.
 * @param status    Trạng thái của thương hiệu (ACTIVE hoặc INACTIVE).
 */
public record BrandManagementItem(
        Long id,
        String brandCode,
        String brandName,
        String country,
        Status status
) {
}
