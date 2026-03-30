package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

/**
 * Record đại diện cho một dòng xe trong giỏ khi tạo đơn bán.
 * Được tạo từ dialog và tập hợp lại trước khi gửi lên service.
 *
 * @param carId    id xe (bắt buộc)
 * @param quantity số lượng cần mua (>= 1)
 * @param unitPrice đơn giá tại thời điểm chốt (lấy từ car.salePrice)
 */
public record OrderDetailRequest(
        Long carId,
        Integer quantity,
        BigDecimal unitPrice
) {
}
