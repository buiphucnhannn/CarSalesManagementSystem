package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;

import java.util.List;

/**
 * Interface cho dịch vụ quản lý Đơn Bán Hàng (Sale Order).
 */
public interface SaleOrderService {

    /**
     * Lấy danh sách tóm tắt tất cả đơn bán, có lọc theo từ khoá và trạng thái.
     */
    List<SaleOrderItem> findOrders(String keyword, OrderStatus statusFilter);

    /**
     * Lấy danh sách chi tiết của một đơn bán.
     */
    List<SaleOrderDetailItem> findDetailsByOrderId(Long orderId);

    /**
     * Lấy dữ liệu nền (metadata) để nạp vào form Tạo Đơn (khách hàng, xe, khuyến mãi, nhân viên).
     */
    SaleOrderMetadata loadMetadata();

    /**
     * Tạo một đơn bán hàng mới.
     * Cập nhật số lượng xe tương ứng.
     */
    void createOrder(CreateOrderRequest request);

    /**
     * Huỷ một đơn bán (Chỉ những đơn chưa PAID).
     * Sẽ hoàn trả lại số lượng xe trống vào trong kho.
     */
    void cancelOrder(Long orderId);
}
