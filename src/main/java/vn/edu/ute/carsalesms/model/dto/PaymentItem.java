package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, được sử dụng để hiển thị thông tin của một lần thanh toán trong danh sách hoặc bảng.
 * Đây là đối tượng chỉ đọc, dùng để truyền dữ liệu đã được xử lý và định dạng sẵn cho tầng giao diện (ví dụ: PaymentPanel).
 *
 * @param id                   Khóa chính của bản ghi thanh toán.
 * @param paymentCode          Mã định danh duy nhất của lần thanh toán.
 * @param orderId              ID của đơn hàng mà lần thanh toán này thuộc về.
 * @param orderCode            Mã của đơn hàng liên quan.
 * @param paymentDate          Ngày và giờ thực hiện thanh toán.
 * @param amount               Số tiền được thanh toán trong lần này.
 * @param paymentMethod        Phương thức được sử dụng cho lần thanh toán này (ví dụ: tiền mặt, chuyển khoản).
 * @param paymentStatus        Trạng thái của lần thanh toán (ví dụ: PENDING - đang chờ, COMPLETED - thành công, FAILED - thất bại).
 * @param transactionReference Mã tham chiếu giao dịch (ví dụ: mã từ ngân hàng), nếu có.
 * @param note                 Ghi chú cho lần thanh toán.
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
