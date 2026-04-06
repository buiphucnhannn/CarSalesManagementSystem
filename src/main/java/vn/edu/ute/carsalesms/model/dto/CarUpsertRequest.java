package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, chứa dữ liệu cần thiết để tạo mới (insert) hoặc cập nhật (update) một chiếc xe.
 * "Upsert" là thuật ngữ kết hợp giữa "Update" và "Insert".
 * Đối tượng này được gửi từ tầng view lên tầng service để thực hiện các thao tác nghiệp vụ.
 *
 * @param id                Khóa chính của xe. Nếu là `null`, đây là yêu cầu tạo mới. Nếu có giá trị, đây là yêu cầu cập nhật.
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
public record CarUpsertRequest(
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
