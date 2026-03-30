package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO cho thực thể Customer.
 * Định nghĩa các thao tác CRUD cơ bản và tìm kiếm theo từ khóa.
 * Tách biệt giao diện khỏi triển khai (Interface Segregation, Dependency Inversion).
 */
public interface CustomerDao {

    /**
     * Tìm kiếm danh sách khách hàng theo từ khóa (mã, tên, số điện thoại, email).
     *
     * @param keyword từ khóa, null hoặc rỗng = lấy tất cả
     * @return danh sách Customer khớp điều kiện
     */
    List<Customer> findCustomers(String keyword);

    /**
     * Tìm khách hàng theo id.
     */
    Optional<Customer> findById(Long id);

    /**
     * Tìm khách hàng theo mã.
     */
    Optional<Customer> findByCode(String customerCode);

    /**
     * Lưu (thêm mới hoặc cập nhật) khách hàng.
     *
     * @param customer entity cần lưu
     * @return entity đã được persist
     */
    Customer save(Customer customer);

    /**
     * Xóa cứng khách hàng khỏi CSDL.
     * Chỉ dùng khi khách hàng chưa có đơn bán.
     *
     * @param id id khách hàng cần xóa
     */
    void deleteById(Long id);
}
