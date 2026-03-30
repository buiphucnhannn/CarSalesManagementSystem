package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record hiển thị thông tin hóa đơn được sinh ra khi đơn bán đã thanh toán đủ.
 *
 * @param id            khoá chính
 * @param invoiceCode   số hóa đơn (duy nhất)
 * @param orderId       id đơn bán tương ứng
 * @param orderCode     mã đơn bán tương ứng
 * @param issuedDate    ngày phát hành hóa đơn
 * @param taxAmount     thuế VAT (10% của finalAmount)
 * @param totalAmount   tổng tiền hóa đơn (finalAmount + taxAmount)
 * @param invoiceStatus trạng thái hóa đơn
 * @param note          ghi chú
 */
public record InvoiceItem(
        Long id,
        String invoiceCode,
        Long orderId,
        String orderCode,
        LocalDateTime issuedDate,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        InvoiceStatus invoiceStatus,
        String note
) {
}
