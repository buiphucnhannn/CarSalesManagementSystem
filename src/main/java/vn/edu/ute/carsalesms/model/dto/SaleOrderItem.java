package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record hiển thị thông tin tóm tắt của một đơn bán trong bảng danh sách.
 * Chỉ đọc (read-only) – dùng để render bảng, không dùng để persist.
 *
 * @param id            khoá chính
 * @param orderCode     mã đơn bán (duy nhất)
 * @param customerName  tên khách hàng
 * @param staffName     tên nhân viên lập đơn
 * @param promotionCode mã khuyến mãi áp dụng (null nếu không có)
 * @param orderDate     ngày lập đơn
 * @param totalAmount   tổng tiền trước giảm
 * @param discountAmount tiền được giảm
 * @param finalAmount   số tiền thực phải trả
 * @param paymentMethod phương thức thanh toán
 * @param orderStatus   trạng thái đơn
 * @param note          ghi chú
 */
public record SaleOrderItem(
        Long id,
        String orderCode,
        String customerName,
        String staffName,
        String promotionCode,
        LocalDateTime orderDate,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        PaymentMethod paymentMethod,
        OrderStatus orderStatus,
        String note
) {
}
