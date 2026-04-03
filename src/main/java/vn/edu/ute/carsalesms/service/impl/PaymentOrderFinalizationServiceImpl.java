package vn.edu.ute.carsalesms.service.impl;

import java.math.BigDecimal;
import java.util.Objects;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.service.InvoiceAutoIssueService;
import vn.edu.ute.carsalesms.service.PaymentOrderFinalizationService;
import vn.edu.ute.carsalesms.service.WarrantyAutoActivationService;

public class PaymentOrderFinalizationServiceImpl implements PaymentOrderFinalizationService {

    private final InvoiceAutoIssueService invoiceAutoIssueService;
    private final WarrantyAutoActivationService warrantyAutoActivationService;

    public PaymentOrderFinalizationServiceImpl(InvoiceAutoIssueService invoiceAutoIssueService,
                                               WarrantyAutoActivationService warrantyAutoActivationService) {
        this.invoiceAutoIssueService = Objects.requireNonNull(invoiceAutoIssueService, "invoiceAutoIssueService is required");
        this.warrantyAutoActivationService = Objects.requireNonNull(warrantyAutoActivationService, "warrantyAutoActivationService is required");
    }

    @Override
    public void finalizeAfterPayment(SaleOrder order, BigDecimal totalPaid) {
        if (totalPaid.compareTo(order.getFinalAmount()) >= 0) {
            order.setOrderStatus(OrderStatus.PAID);
            invoiceAutoIssueService.createIfAbsent(order);
            warrantyAutoActivationService.activateForPaidOrder(order);
            return;
        }
        if (order.getOrderStatus() == OrderStatus.PENDING && totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }
    }
}

