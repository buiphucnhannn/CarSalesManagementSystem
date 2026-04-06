package vn.edu.ute.carsalesms.model.dto;

/**
 * DTO (Data Transfer Object) dạng record, được thiết kế đặc biệt để sử dụng trong các thành phần UI
 * cho phép người dùng tìm kiếm và chọn một chiếc xe, ví dụ như ComboBox hoặc danh sách tìm kiếm.
 * Nó chỉ chứa những thông tin tối thiểu cần thiết để định danh và hiển thị.
 *
 * @param id   Khóa chính của xe.
 * @param code Mã xe.
 * @param name Tên xe.
 */
public record CarLookupItem(
        Long id,
        String code,
        String name
) {
    /**
     * Ghi đè phương thức `toString()` để khi đối tượng này được thêm vào các component Swing
     * (như JComboBox), nó sẽ hiển thị một chuỗi thân thiện với người dùng.
     *
     * @return Chuỗi có định dạng "Mã xe - Tên xe".
     */
    @Override
    public String toString() {
        return code + " - " + name;
    }
}
