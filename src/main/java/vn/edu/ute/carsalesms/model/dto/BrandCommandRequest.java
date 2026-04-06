package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, dùng để đóng gói dữ liệu cho các lệnh (command)
 * liên quan đến việc tạo mới hoặc cập nhật một thương hiệu xe.
 *
 * @param id        Khóa chính của thương hiệu. Nếu là `null`, đây là lệnh tạo mới. Nếu có giá trị, đây là lệnh cập nhật.
 * @param brandCode Mã thương hiệu.
 * @param brandName Tên thương hiệu.
 * @param country   Quốc gia của thương hiệu.
 * @param status    Trạng thái của thương hiệu (ACTIVE hoặc INACTIVE).
 */
public record BrandCommandRequest(
        Long id,
        String brandCode,
        String brandName,
        String country,
        Status status
) {
}
