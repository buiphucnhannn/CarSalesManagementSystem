package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin của một kỳ hạn thanh toán trong kế hoạch trả góp.
 *
 * @param id              Khóa chính của kỳ hạn.
 * @param saleOrderId     ID của đơn hàng trả góp.
 * @param orderCode       Mã của đơn hàng trả góp.
 * @param customerName    Tên của khách hàng.
 * @param installmentNo   Số thứ tự của kỳ hạn (ví dụ: kỳ 1, kỳ 2).
 * @param dueDate         Ngày đến hạn thanh toán của kỳ này.
 * @param amount          Số tiền phải trả cho kỳ này.
 * @param paidAmount      Số tiền đã thực trả cho kỳ này.
 * @param status          Trạng thái của kỳ hạn (ví dụ: UNPAID, PAID, OVERDUE).
 * @param note            Ghi chú cho kỳ hạn.
 */
public record InstallmentItem(
        Long id,
        Long saleOrderId,
        String orderCode,
        String customerName,
        Integer installmentNo,
        LocalDate dueDate,
        BigDecimal amount,
        BigDecimal paidAmount,
        InstallmentStatus status,
        String note
) {
    /**
     * Phương thức tiện ích để tính toán số tiền còn lại phải trả cho kỳ hạn này.
     * @return Số tiền còn nợ (amount - paidAmount).
     */
    public BigDecimal getDueRemaining() {
        return amount.subtract(paidAmount);
    }
}
