package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Record DTO cho dữ liệu hiển thị một kỳ đóng tiền trả góp.
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
     * Số tiền còn nợ của kỳ này
     */
    public BigDecimal getDueRemaining() {
        return amount.subtract(paidAmount);
    }
}
