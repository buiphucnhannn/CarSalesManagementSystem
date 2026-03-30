package vn.edu.ute.carsalesms.model.dto;

import java.util.List;

/**
 * Metadata cần thiết khi mở dialog tạo đơn bán mới.
 * Cung cấp toàn bộ lookup data để điền ComboBox và bảng chọn xe.
 *
 * @param customers   danh sách khách hàng (id, code, name)
 * @param staffs      danh sách nhân viên đang ACTIVE
 * @param promotions  danh sách KM còn trong thời hạn, status = ACTIVE
 * @param cars        danh sách xe còn hàng (availableQuantity > 0)
 */
public record SaleOrderMetadata(
        List<CarLookupItem> customers,
        List<CarLookupItem> staffs,
        List<CarLookupItem> promotions,
        List<CarLookupItem> cars
) {
    /** Metadata rỗng dùng khi không load được dữ liệu. */
    public static SaleOrderMetadata empty() {
        return new SaleOrderMetadata(List.of(), List.of(), List.of(), List.of());
    }
}
