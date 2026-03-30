package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.entity.SaleOrderDetail;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO cho SaleOrder và SaleOrderDetail.
 * Tách giao diện khỏi triển khai – Dependency Inversion Principle.
 */
public interface SaleOrderDao {

    /**
     * Tìm danh sách đơn bán theo từ khóa và trạng thái.
     *
     * @param keyword      tìm theo mã đơn / tên khách / tên nhân viên
     * @param statusFilter lọc theo OrderStatus (null = tất cả)
     * @return danh sách SaleOrder có eager-fetch customer, staff, promotion
     */
    List<SaleOrder> findOrders(String keyword, OrderStatus statusFilter);

    /**
     * Tìm đơn bán theo id, eager-fetch tất cả liên kết cần thiết.
     */
    Optional<SaleOrder> findById(Long id);

    /**
     * Kiểm tra mã đơn đã tồn tại chưa.
     */
    boolean existsByCode(String orderCode);

    /**
     * Lưu (thêm mới hoặc cập nhật) đơn bán.
     */
    SaleOrder save(SaleOrder order);

    /**
     * Lấy danh sách chi tiết của đơn bán, eager-fetch Car.
     */
    List<SaleOrderDetail> findDetailsByOrderId(Long orderId);

    /**
     * Lưu một dòng chi tiết đơn bán.
     */
    SaleOrderDetail saveDetail(SaleOrderDetail detail);
}
