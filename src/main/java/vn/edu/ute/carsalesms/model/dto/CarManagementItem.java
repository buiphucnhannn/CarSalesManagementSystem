package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin chi tiết của một xe trong giao diện quản lý.
 * Dữ liệu được tổng hợp từ thực thể `Car` và các thực thể liên quan như `Brand`, `CarCategory`, `Branch`.
 *
 * @param id                Khóa chính của xe.
 * @param carCode           Mã xe.
 * @param carName           Tên xe.
 * @param brandId           ID của thương hiệu.
 * @param brandName         Tên của thương hiệu.
 * @param categoryId        ID của danh mục.
 * @param categoryName      Tên của danh mục.
 * @param branchId          ID của chi nhánh.
 * @param branchName        Tên của chi nhánh.
 * @param importPrice       Giá nhập kho.
 * @param salePrice         Giá bán niêm yết.
 * @param quantity          Tổng số lượng đã nhập.
 * @param availableQuantity Số lượng hiện có sẵn để bán.
 * @param status            Trạng thái của xe (ACTIVE hoặc INACTIVE).
 */
public record CarManagementItem(
        Long id,
        String carCode,
        String carName,
        Long brandId,
        String brandName,
        Long categoryId,
        String categoryName,
        Long branchId,
        String branchName,
        BigDecimal importPrice,
        BigDecimal salePrice,
        Integer quantity,
        Integer availableQuantity,
        Status status
) {
}
