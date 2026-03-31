package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.CustomerCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CustomerItem;

import java.util.List;

/**
 * Interface dịch vụ quản lý khách hàng.
 * Định nghĩa các use-case nghiệp vụ (Open/Closed Principle):
 * implementation có thể thay đổi mà không ảnh hưởng đến caller.
 */
public interface CustomerService {

    /**
     * Lấy danh sách khách hàng theo từ khóa tìm kiếm.
     *
     * @param keyword từ khóa (null = tất cả)
     * @return danh sách CustomerItem đã mapping từ entity
     */
    List<CustomerItem> getCustomers(String keyword);

    /**
     * Sinh mã khách hàng kế tiếp theo dữ liệu hiện có.
     */
    String getNextCustomerCode();

    /**
     * Thêm mới khách hàng sau khi validate.
     *
     * @param request dữ liệu từ dialog
     * @return CustomerItem đã được persist
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc mã trùng
     */
    CustomerItem createCustomer(CustomerCommandRequest request);

    /**
     * Cập nhật thông tin khách hàng.
     *
     * @param request dữ liệu từ dialog, phải chứa id hợp lệ
     * @return CustomerItem đã cập nhật
     * @throws IllegalArgumentException nếu không tìm thấy hoặc mã trùng
     */
    CustomerItem updateCustomer(CustomerCommandRequest request);

    /**
     * Xóa khách hàng.
     * Sẽ thất bại nếu khách hàng có đơn bán liên quan.
     *
     * @param customerId id khách hàng cần xóa
     * @throws IllegalArgumentException  nếu không tìm thấy
     * @throws IllegalStateException     nếu khách hàng còn ràng buộc dữ liệu
     */
    void deleteCustomer(Long customerId);
}
