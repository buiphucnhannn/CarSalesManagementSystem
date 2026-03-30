package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

/**
 * Record hiển thị một dòng chi tiết trong đơn bán.
 *
 * @param id             khoá chính của SaleOrderDetail
 * @param carId          id xe
 * @param carCode        mã xe
 * @param carName        tên xe
 * @param quantity       số lượng đặt mua
 * @param unitPrice      đơn giá tại thời điểm lập đơn
 * @param discountAmount tiền giảm của dòng này
 * @param lineTotal      thành tiền = quantity × unitPrice - discountAmount
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
