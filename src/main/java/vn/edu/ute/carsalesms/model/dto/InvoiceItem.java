package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, được sử dụng để hiển thị thông tin của một hóa đơn.
 * Hóa đơn thường được tạo ra sau khi một đơn hàng đã được thanh toán đầy đủ.
 *
 * @param id            Khóa chính của hóa đơn.
 * @param invoiceCode   Số hóa đơn (mã định danh duy nhất).
 * @param orderId       ID của đơn hàng (SaleOrder) mà hóa đơn này được phát hành cho.
 * @param orderCode     Mã của đơn hàng tương ứng.
 * @param issuedDate    Ngày phát hành hóa đơn.
 * @param taxAmount     Số tiền thuế (ví dụ: VAT 10%) được tính trên giá trị cuối cùng của đơn hàng.
 * @param totalAmount   Tổng số tiền trên hóa đơn, bao gồm cả giá trị đơn hàng và thuế.
 * @param invoiceStatus Trạng thái của hóa đơn (ví dụ: PENDING, PAID, CANCELLED).
 * @param note          Ghi chú kèm theo hóa đơn.
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
