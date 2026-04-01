package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.PaymentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.PaymentService;
import vn.edu.ute.carsalesms.service.impl.NoOpAuditLogService;

import java.util.List;
import java.util.Objects;

/**
 * Controller làm trung gian giữa PaymentPanel và PaymentService.
 */
public class PaymentController {

    private final PaymentService paymentService;
    private final AuditLogService auditLogService;

    public PaymentController(PaymentService paymentService) {
        this(paymentService, new NoOpAuditLogService());
    }

    public PaymentController(PaymentService paymentService, AuditLogService auditLogService) {
        this.paymentService = Objects.requireNonNull(paymentService, "paymentService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    public List<PaymentItem> findPaymentsByOrderId(Long orderId) {
        return paymentService.findPaymentsByOrderId(orderId);
    }

    public void addPayment(PaymentRequest request) {
        paymentService.addPayment(request);
        auditLogService.log("CREATE", "PAYMENT", request.orderId(), null, request.toString());
    }
}
