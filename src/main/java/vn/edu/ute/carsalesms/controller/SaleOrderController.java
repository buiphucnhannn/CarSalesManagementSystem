package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;
import vn.edu.ute.carsalesms.service.SaleOrderService;

import java.util.List;
import java.util.Objects;

/**
 * SaleOrderController xử lý các yêu cầu liên quan đến đơn đặt hàng.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic đơn đặt hàng.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào các giao diện
 * (SaleOrderService, AuditLogService) thay vì các triển khai cụ thể.
 */
public class SaleOrderController {

    private final SaleOrderService saleOrderService;
    private final AuditLogService auditLogService;

    /**
     * Xây dựng một SaleOrderController mới với SaleOrderService đã cho.
     * @param saleOrderService dịch vụ sẽ được sử dụng để quản lý đơn đặt hàng.
     */
    public SaleOrderController(SaleOrderService saleOrderService) {
        this(saleOrderService, new NoOpAuditLogService());
    }

    /**
     * Xây dựng một SaleOrderController mới với SaleOrderService và AuditLogService đã cho.
     * @param saleOrderService dịch vụ sẽ được sử dụng để quản lý đơn đặt hàng.
     * @param auditLogService dịch vụ sẽ được sử dụng để ghi lại các hành động.
     */
    public SaleOrderController(SaleOrderService saleOrderService, AuditLogService auditLogService) {
        this.saleOrderService = Objects.requireNonNull(saleOrderService, "saleOrderService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    /**
     * Tìm tất cả các đơn đặt hàng khớp với từ khóa và bộ lọc trạng thái đã cho.
     * @param keyword từ khóa để tìm kiếm.
     * @param statusFilter bộ lọc trạng thái.
     * @return danh sách các mục đơn đặt hàng.
     */
    public List<SaleOrderItem> findOrders(String keyword, OrderStatus statusFilter) {
        return saleOrderService.findOrders(keyword, statusFilter);
    }

    /**
     * Tìm tất cả các chi tiết cho một đơn đặt hàng cụ thể.
     * @param orderId ID của đơn đặt hàng để tìm kiếm chi tiết.
     * @return danh sách các mục chi tiết đơn đặt hàng.
     */
    public List<SaleOrderDetailItem> findDetailsByOrderId(Long orderId) {
        return saleOrderService.findDetailsByOrderId(orderId);
    }

    /**
     * Tải siêu dữ liệu cho các đơn đặt hàng.
     * @return siêu dữ liệu đơn đặt hàng.
     */
    public SaleOrderMetadata loadMetadata() {
        return saleOrderService.loadMetadata();
    }

    /**
     * Tạo một đơn đặt hàng mới.
     * @param request yêu cầu tạo đơn đặt hàng.
     */
    public void createOrder(CreateOrderRequest request) {
        saleOrderService.createOrder(request);
        auditLogService.log("CREATE", "SALE_ORDER", null, null, request.toString());
    }

    /**
     * Hủy một đơn đặt hàng hiện có.
     * @param orderId ID của đơn đặt hàng cần hủy.
     */
    public void cancelOrder(Long orderId) {
        saleOrderService.cancelOrder(orderId);
        auditLogService.log("CANCEL", "SALE_ORDER", orderId, null, "status=CANCELLED");
    }
}
