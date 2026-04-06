package vn.edu.ute.carsalesms.model.dto;

/**
 * DTO (Data Transfer Object) dạng record, đại diện cho một lựa chọn (option) trong các danh sách
 * hoặc ComboBox trên giao diện đặt lịch lái thử.
 *
 * @param id          ID của đối tượng (ví dụ: ID của khách hàng, ID của xe).
 * @param displayName Tên hiển thị cho người dùng (ví dụ: tên khách hàng, tên xe).
 */
public record TestDriveLookupOption(Long id, String displayName) {
}
