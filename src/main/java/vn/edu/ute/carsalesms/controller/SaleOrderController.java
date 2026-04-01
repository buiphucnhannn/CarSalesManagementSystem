package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.SaleOrderService;
import vn.edu.ute.carsalesms.service.impl.NoOpAuditLogService;

import java.util.List;
import java.util.Objects;

/**
 * Controller làm trung gian giữa SaleOrderPanel và SaleOrderService.
 */
public class SaleOrderController {

    private final SaleOrderService saleOrderService;
    private final AuditLogService auditLogService;

    public SaleOrderController(SaleOrderService saleOrderService) {
        this(saleOrderService, new NoOpAuditLogService());
    }

    public SaleOrderController(SaleOrderService saleOrderService, AuditLogService auditLogService) {
        this.saleOrderService = Objects.requireNonNull(saleOrderService, "saleOrderService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    public List<SaleOrderItem> findOrders(String keyword, OrderStatus statusFilter) {
        return saleOrderService.findOrders(keyword, statusFilter);
    }

    public List<SaleOrderDetailItem> findDetailsByOrderId(Long orderId) {
        return saleOrderService.findDetailsByOrderId(orderId);
    }

    public SaleOrderMetadata loadMetadata() {
        return saleOrderService.loadMetadata();
    }

    public void createOrder(CreateOrderRequest request) {
        saleOrderService.createOrder(request);
        auditLogService.log("CREATE", "SALE_ORDER", null, null, request.toString());
    }

    public void cancelOrder(Long orderId) {
        saleOrderService.cancelOrder(orderId);
        auditLogService.log("CANCEL", "SALE_ORDER", orderId, null, "status=CANCELLED");
    }
}
