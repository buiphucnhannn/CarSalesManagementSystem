package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin tóm tắt của một phiếu bảo hành.
 * Dữ liệu được tổng hợp từ nhiều thực thể liên quan (Warranty, SaleOrder, Customer, Car) để tiện cho việc hiển thị.
 *
 * @param id              Khóa chính của phiếu bảo hành.
 * @param warrantyCode    Mã của phiếu bảo hành.
 * @param saleOrderCode   Mã của đơn hàng đã phát sinh ra phiếu bảo hành này.
 * @param customerName    Tên của khách hàng sở hữu phiếu bảo hành.
 * @param carModel        Tên/mẫu của chiếc xe được bảo hành.
 * @param startDate       Ngày bắt đầu hiệu lực của bảo hành.
 * @param endDate         Ngày hết hạn bảo hành.
 * @param warrantyStatus  Trạng thái hiện tại của phiếu bảo hành (ví dụ: ACTIVE, EXPIRED).
 * @param note            Ghi chú về phiếu bảo hành.
 */
public record WarrantyItem(
        Long id,
        String warrantyCode,
        String saleOrderCode,
        String customerName,
        String carModel,
        LocalDate startDate,
        LocalDate endDate,
        WarrantyStatus warrantyStatus,
        String note
) {
}
