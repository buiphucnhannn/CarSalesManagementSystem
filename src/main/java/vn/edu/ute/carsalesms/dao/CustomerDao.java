package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Khách hàng (Customer).
 * Định nghĩa các phương thức trừu tượng cho các thao tác CRUD (Tạo, Đọc, Cập nhật, Xóa) cơ bản
 * và các phương thức tìm kiếm.
 * Việc sử dụng interface giúp tuân thủ các nguyên tắc SOLID như Tách biệt Giao diện (Interface Segregation)
 * và Đảo ngược Phụ thuộc (Dependency Inversion), làm cho code linh hoạt và dễ bảo trì hơn.
 */
public interface CustomerDao {

    /**
     * Tìm kiếm và trả về danh sách khách hàng dựa trên một từ khóa.
     * Từ khóa có thể được dùng để tìm kiếm trên nhiều trường như mã khách hàng, tên, số điện thoại, hoặc email.
     *
     * @param keyword Từ khóa tìm kiếm. Nếu là null hoặc chuỗi rỗng, phương thức sẽ trả về tất cả khách hàng.
     * @return Danh sách các đối tượng Customer phù hợp với điều kiện tìm kiếm.
     */
    List<Customer> findCustomers(String keyword);

    /**
     * Tìm một khách hàng dựa trên ID của họ.
     *
     * @param id ID của khách hàng cần tìm.
     * @return Một Optional chứa đối tượng Customer nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Customer> findById(Long id);

    /**
     * Tìm một khách hàng dựa trên mã khách hàng (customerCode).
     *
     * @param customerCode Mã định danh duy nhất của khách hàng.
     * @return Một Optional chứa đối tượng Customer nếu tìm thấy.
     */
    Optional<Customer> findByCode(String customerCode);

    /**
     * Lưu (thêm mới hoặc cập nhật) thông tin của một khách hàng.
     *
     * @param customer Đối tượng Customer cần lưu.
     * @return Đối tượng Customer sau khi đã được lưu vào cơ sở dữ liệu (persisted).
     */
    Customer save(Customer customer);

    /**
     * Xóa một khách hàng khỏi cơ sở dữ liệu một cách vĩnh viễn (hard delete).
     * Chú ý: Thao tác này chỉ nên được thực hiện khi chắc chắn rằng khách hàng không có các ràng buộc dữ liệu quan trọng,
     * ví dụ như chưa từng có đơn hàng nào.
     *
     * @param id ID của khách hàng cần xóa.
     */
    void deleteById(Long id);
}
