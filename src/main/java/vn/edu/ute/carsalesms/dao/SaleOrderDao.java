package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.entity.SaleOrderDetail;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho các thực thể Đơn hàng (SaleOrder) và Chi tiết đơn hàng (SaleOrderDetail).
 * Việc sử dụng interface giúp tách biệt logic truy cập dữ liệu khỏi logic nghiệp vụ,
 * tuân thủ Nguyên tắc Đảo ngược Phụ thuộc (Dependency Inversion Principle).
 */
public interface SaleOrderDao {

    /**
     * Tìm kiếm và trả về danh sách các đơn hàng dựa trên từ khóa và bộ lọc trạng thái.
     *
     * @param keyword      Từ khóa để tìm kiếm (ví dụ: mã đơn hàng, tên khách hàng, tên nhân viên).
     * @param statusFilter Lọc theo trạng thái của đơn hàng (ví dụ: PENDING, COMPLETED). Nếu null, không lọc theo trạng thái.
     * @return Danh sách các đối tượng SaleOrder, bao gồm thông tin khách hàng, nhân viên và khuyến mãi đã được tải sẵn (eager-fetch).
     */
    List<SaleOrder> findOrders(String keyword, OrderStatus statusFilter);

    /**
     * Tìm một đơn hàng dựa trên ID của nó.
     * Phương thức này sẽ tải tất cả các thực thể liên quan cần thiết (eager-fetch) để đảm bảo đối tượng SaleOrder có đầy đủ thông tin.
     *
     * @param id ID của đơn hàng cần tìm.
     * @return Một Optional chứa đối tượng SaleOrder nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<SaleOrder> findById(Long id);

    /**
     * Kiểm tra xem một mã đơn hàng đã tồn tại trong cơ sở dữ liệu hay chưa.
     *
     * @param orderCode Mã đơn hàng cần kiểm tra.
     * @return `true` nếu mã đã tồn tại, `false` nếu chưa.
     */
    boolean existsByCode(String orderCode);

    /**
     * Lưu (thêm mới hoặc cập nhật) một đơn hàng.
     *
     * @param order Đối tượng SaleOrder cần lưu.
     * @return Đối tượng SaleOrder sau khi đã được lưu.
     */
    SaleOrder save(SaleOrder order);

    /**
     * Lấy danh sách các dòng chi tiết của một đơn hàng cụ thể.
     *
     * @param orderId ID của đơn hàng.
     * @return Danh sách các đối tượng SaleOrderDetail, bao gồm thông tin về xe (Car) đã được tải sẵn (eager-fetch).
     */
    List<SaleOrderDetail> findDetailsByOrderId(Long orderId);

    /**
     * Lưu một dòng chi tiết đơn hàng.
     *
     * @param detail Đối tượng SaleOrderDetail cần lưu.
     * @return Đối tượng SaleOrderDetail sau khi đã được lưu.
     */
    SaleOrderDetail saveDetail(SaleOrderDetail detail);
}
