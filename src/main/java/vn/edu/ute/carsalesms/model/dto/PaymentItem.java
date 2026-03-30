package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record hiển thị thông tin một lần thanh toán trong bảng.
 * Chỉ đọc – dùng để render bảng PaymentPanel.
 *
 * @param id                   khoá chính
 * @param paymentCode          mã thanh toán
 * @param orderId              id đơn bán liên kết
 * @param orderCode            mã đơn bán liên kết
 * @param paymentDate          ngày giờ thanh toán
 * @param amount               số tiền thanh toán lần này
 * @param paymentMethod        phương thức thanh toán của lần này
 * @param paymentStatus        trạng thái: PENDING / COMPLETED / FAILED
 * @param transactionReference mã giao dịch ngân hàng (tùy chọn)
 * @param note                 ghi chú
 */
public record PaymentItem(
        Long id,
        String paymentCode,
        Long orderId,
        String orderCode,
        LocalDateTime paymentDate,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String transactionReference,
        String note
) {
}
