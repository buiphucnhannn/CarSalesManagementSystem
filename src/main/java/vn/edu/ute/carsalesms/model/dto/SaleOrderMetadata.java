package vn.edu.ute.carsalesms.model.dto;

import java.util.List;

/**
 * DTO (Data Transfer Object) dạng record, chứa tất cả các dữ liệu "siêu dữ liệu" (metadata)
 * cần thiết để khởi tạo và hiển thị giao diện tạo/sửa một đơn hàng mới.
 * Việc gom nhóm các dữ liệu này vào một DTO giúp giảm số lần gọi từ view xuống service.
 *
 * @param customers   Danh sách khách hàng có sẵn trong hệ thống để người dùng lựa chọn.
 * @param staffs      Danh sách các nhân viên đang hoạt động để gán cho đơn hàng.
 * @param promotions  Danh sách các chương trình khuyến mãi đang có hiệu lực.
 * @param cars        Danh sách các xe đang có sẵn hàng (availableQuantity > 0) để thêm vào đơn hàng.
 */
public record SaleOrderMetadata(
        List<CarLookupItem> customers,
        List<CarLookupItem> staffs,
        List<CarLookupItem> promotions,
        List<CarLookupItem> cars
) {
    /**
     * Phương thức factory tĩnh để tạo một đối tượng `SaleOrderMetadata` rỗng.
     * Rất hữu ích để tránh lỗi NullPointerException khi dữ liệu chưa kịp tải hoặc tải thất bại.
     * @return Một đối tượng `SaleOrderMetadata` với tất cả các danh sách đều rỗng.
     */
    public static SaleOrderMetadata empty() {
        return new SaleOrderMetadata(List.of(), List.of(), List.of(), List.of());
    }
}
