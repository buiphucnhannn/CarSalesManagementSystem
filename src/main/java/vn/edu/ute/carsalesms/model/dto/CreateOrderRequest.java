package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.PaymentMethod;

import java.util.List;

/**
 * Record mang toàn bộ thông tin cần thiết để tạo một đơn bán mới.
 * Được tạo từ SaleOrderCreateDialog và gửi lên SaleOrderService.
 *
 * @param customerId    id khách hàng (bắt buộc)
 * @param staffId       id nhân viên lập đơn (bắt buộc)
 * @param promotionId   id khuyến mãi áp dụng (null = không áp dụng)
 * @param paymentMethod phương thức thanh toán chính của đơn (bắt buộc)
 * @param details       danh sách xe cần mua (>= 1 dòng)
 * @param note          ghi chú tùy chọn
 */
public record CreateOrderRequest(
        Long customerId,
        Long staffId,
        Long promotionId,
        PaymentMethod paymentMethod,
        List<OrderDetailRequest> details,
        String note
) {
}
