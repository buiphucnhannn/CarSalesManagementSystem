package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin của một dòng chi tiết trong đơn hàng.
 *
 * @param id             Khóa chính của chi tiết đơn hàng (SaleOrderDetail).
 * @param carId          ID của xe trong dòng chi tiết này.
 * @param carCode        Mã của xe.
 * @param carName        Tên của xe.
 * @param quantity       Số lượng xe được đặt mua.
 * @param unitPrice      Đơn giá của xe tại thời điểm lập đơn.
 * @param discountAmount Số tiền được giảm giá trên dòng này (nếu có).
 * @param lineTotal      Thành tiền của dòng này, được tính bằng: `quantity * unitPrice - discountAmount`.
 */
public record SaleOrderDetailItem(
        Long id,
        Long carId,
        String carCode,
        String carName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal lineTotal
) {
}
