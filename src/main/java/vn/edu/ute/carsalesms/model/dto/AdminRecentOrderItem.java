package vn.edu.ute.carsalesms.model.dto;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị một mục trong danh sách "Đơn hàng gần đây"
 * trên bảng điều khiển (dashboard) của quản trị viên.
 *
 * @param orderCode    Mã của đơn hàng.
 * @param customerName Tên của khách hàng đã đặt đơn.
 * @param carName      Tên của chiếc xe trong đơn hàng (thường là xe đầu tiên nếu có nhiều xe).
 * @param status       Trạng thái hiện tại của đơn hàng.
 */
public record AdminRecentOrderItem(
        String orderCode,
        String customerName,
        String carName,
        String status
) {
}
