package vn.edu.ute.carsalesms.model.dto;

import java.util.List;

/**
 * Metadata cần thiết khi mở dialog thêm/sửa nhân viên.
 * Chứa danh sách chi nhánh để điền vào ComboBox.
 *
 * @param branches danh sách chi nhánh đang hoạt động
 */
public record StaffManagementMetadata(
        List<CarLookupItem> branches
) {
    /**
     * Trả về metadata rỗng dùng khi không load được dữ liệu.
     */
    public static StaffManagementMetadata empty() {
        return new StaffManagementMetadata(List.of());
    }
}
