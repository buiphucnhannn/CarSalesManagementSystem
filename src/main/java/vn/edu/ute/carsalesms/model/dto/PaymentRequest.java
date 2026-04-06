package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.PaymentMethod;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) dạng record, được sử dụng để mang dữ liệu yêu cầu tạo một lần thanh toán mới.
 * Đối tượng này thường được tạo ra ở tầng giao diện (ví dụ: từ một dialog nhập liệu) và được gửi đến tầng service để xử lý.
 *
 * @param orderId              ID của đơn hàng cần thực hiện thanh toán (bắt buộc).
 * @param amount               Số tiền được thanh toán trong lần này (phải lớn hơn 0).
 * @param paymentMethod        Phương thức thanh toán được sử dụng (bắt buộc).
 * @param transactionReference Mã tham chiếu giao dịch, ví dụ như mã từ ngân hàng (tùy chọn).
 * @param note                 Ghi chú cho lần thanh toán này (tùy chọn).
 * @param installmentMonths    Số tháng trả góp. Trường này chỉ có giá trị khi phương thức thanh toán là `INSTALLMENT`.
 *                             Nếu là thanh toán trực tiếp (CASH, BANK_TRANSFER), giá trị này sẽ là null hoặc 0.
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
