package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.service.SaleOrderService;

import java.util.List;

/**
 * Controller làm trung gian giữa SaleOrderPanel và SaleOrderService.
 */
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    public SaleOrderController(SaleOrderService saleOrderService) {
        this.saleOrderService = saleOrderService;
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
    }

    public void cancelOrder(Long orderId) {
        saleOrderService.cancelOrder(orderId);
    }
}
