package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.PaymentMethod;

import java.math.BigDecimal;

/**
 * Record mang dữ liệu khi ghi nhận một lần thanh toán mới.
 * Được tạo từ PaymentDialog và gửi lên PaymentService.
 *
 * @param orderId              id đơn bán cần thanh toán (bắt buộc)
 * @param amount               số tiền thanh toán lần này (> 0)
 * @param paymentMethod        phương thức thanh toán lần này (bắt buộc)
 * @param transactionReference mã giao dịch (tùy chọn)
 * @param note                 ghi chú (tùy chọn)
 * @param installmentMonths    số kỳ hạn trả góp tính bằng tháng (chỉ dùng khi method là INSTALLMENT, null/0 nếu là CASH/BANK)
 */
public record PaymentRequest(
        Long orderId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String transactionReference,
        String note,
        Integer installmentMonths
) {
}
