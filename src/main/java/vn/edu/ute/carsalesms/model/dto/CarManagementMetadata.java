package vn.edu.ute.carsalesms.model.dto;

import java.util.List;

/**
 * DTO (Data Transfer Object) dạng record, chứa các "siêu dữ liệu" (metadata) cần thiết cho các chức năng quản lý xe,
 * thương hiệu, và danh mục. Việc gom nhóm này giúp giảm số lần gọi API từ tầng view.
 *
 * @param brands           Danh sách các thương hiệu đang hoạt động, dùng để điền vào ComboBox.
 * @param categories       Danh sách các danh mục xe đang hoạt động, dùng để điền vào ComboBox.
 * @param branches         Danh sách các chi nhánh đang hoạt động, dùng để điền vào ComboBox.
 * @param nextCarCode      Mã xe được hệ thống gợi ý cho lần tạo tiếp theo.
 * @param nextBrandCode    Mã thương hiệu được hệ thống gợi ý cho lần tạo tiếp theo.
 * @param nextCategoryCode Mã danh mục được hệ thống gợi ý cho lần tạo tiếp theo.
 */
public record CarManagementMetadata(
        List<CarLookupItem> brands,
        List<CarLookupItem> categories,
        List<CarLookupItem> branches,
        String nextCarCode,
        String nextBrandCode,
        String nextCategoryCode
) {
    /**
     * Phương thức factory tĩnh để tạo một đối tượng `CarManagementMetadata` rỗng với các giá trị mặc định.
     * @return Một đối tượng `CarManagementMetadata` rỗng.
     */
    public static CarManagementMetadata empty() {
        return new CarManagementMetadata(List.of(), List.of(), List.of(), "CAR-0001", "BRAND-0001", "CAT-0001");
    }
}
