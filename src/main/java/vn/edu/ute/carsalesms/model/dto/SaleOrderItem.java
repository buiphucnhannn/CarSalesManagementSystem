package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, được sử dụng để hiển thị thông tin tóm tắt của một đơn hàng trong bảng danh sách.
 * Đây là đối tượng chỉ đọc, tổng hợp dữ liệu từ nhiều thực thể (SaleOrder, Customer, Staff, Promotion) để tiện cho việc hiển thị.
 * Nó không được dùng để lưu trữ hay cập nhật dữ liệu.
 *
 * @param id            Khóa chính của đơn hàng.
 * @param orderCode     Mã đơn hàng (duy nhất).
 * @param customerName  Tên của khách hàng đặt hàng.
 * @param staffName     Tên của nhân viên tạo đơn hàng.
 * @param promotionCode Mã của chương trình khuyến mãi được áp dụng (có thể là null nếu không có).
 * @param orderDate     Ngày và giờ tạo đơn hàng.
 * @param totalAmount   Tổng giá trị của đơn hàng trước khi áp dụng giảm giá.
 * @param discountAmount Số tiền được giảm giá.
 * @param finalAmount   Số tiền cuối cùng mà khách hàng phải trả.
 * @param paymentMethod Phương thức thanh toán chính của đơn hàng.
 * @param orderStatus   Trạng thái hiện tại của đơn hàng (ví dụ: PENDING, COMPLETED, CANCELLED).
 * @param note          Ghi chú của đơn hàng.
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
