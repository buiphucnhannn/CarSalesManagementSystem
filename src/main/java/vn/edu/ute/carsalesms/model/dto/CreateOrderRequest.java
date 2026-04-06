package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.PaymentMethod;

import java.util.List;

/**
 * DTO (Data Transfer Object) dạng record, chứa toàn bộ thông tin cần thiết để tạo một đơn hàng mới.
 * Đối tượng này được tạo ra từ giao diện (ví dụ: SaleOrderCreateDialog) và được gửi đến tầng service để xử lý nghiệp vụ.
 *
 * @param customerId    ID của khách hàng (bắt buộc).
 * @param staffId       ID của nhân viên lập đơn (bắt buộc).
 * @param promotionId   ID của chương trình khuyến mãi được áp dụng (có thể là null nếu không áp dụng).
 * @param paymentMethod Phương thức thanh toán chính cho đơn hàng (bắt buộc).
 * @param details       Danh sách các chi tiết đơn hàng, mỗi chi tiết tương ứng với một loại xe và số lượng mua. Phải có ít nhất một chi tiết.
 * @param note          Ghi chú chung cho toàn bộ đơn hàng (tùy chọn).
 */
public record CreateOrderRequest(
        Long customerId,
        Long staffId,
        Long promotionId,
        PaymentMethod paymentMethod,
        List<OrderDetailRequest> details,
        String note
) {
}
