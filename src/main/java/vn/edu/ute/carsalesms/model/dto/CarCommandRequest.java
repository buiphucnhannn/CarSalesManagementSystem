package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, được sử dụng để đóng gói dữ liệu cho các lệnh (command)
 * liên quan đến việc tạo mới hoặc cập nhật thông tin xe.
 * Tên "CommandRequest" nhấn mạnh rằng đây là một yêu cầu để thực hiện một hành động thay đổi dữ liệu.
 *
 * @param id                Khóa chính của xe. Nếu là `null`, đây là lệnh tạo mới. Nếu có giá trị, đây là lệnh cập nhật.
 * @param carCode           Mã xe.
 * @param carName           Tên xe.
 * @param brandId           ID của thương hiệu xe.
 * @param categoryId        ID của danh mục xe.
 * @param branchId          ID của chi nhánh quản lý xe.
 * @param importPrice       Giá nhập kho.
 * @param salePrice         Giá bán niêm yết.
 * @param quantity          Tổng số lượng nhập.
 * @param availableQuantity Số lượng có sẵn để bán.
 * @param status            Trạng thái của xe (ACTIVE hoặc INACTIVE).
 */
public record CarCommandRequest(
        Long id,
        String carCode,
        String carName,
        Long brandId,
        Long categoryId,
        Long branchId,
        BigDecimal importPrice,
        BigDecimal salePrice,
        Integer quantity,
        Integer availableQuantity,
        Status status
) {
}
