package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, chứa dữ liệu cho lệnh tạo mới hoặc cập nhật một danh mục xe.
 *
 * @param id           ID của danh mục. Nếu là `null`, đây là yêu cầu tạo mới. Nếu có giá trị, đây là yêu cầu cập nhật.
 * @param categoryCode Mã danh mục (duy nhất).
 * @param categoryName Tên danh mục.
 * @param status       Trạng thái của danh mục (ACTIVE hoặc INACTIVE).
 */
public record CategoryCommandRequest(
        Long id,
        String categoryCode,
        String categoryName,
        Status status
) {
}
