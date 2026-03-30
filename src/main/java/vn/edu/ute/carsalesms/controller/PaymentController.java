package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.PaymentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.service.PaymentService;

import java.util.List;

/**
 * Controller làm trung gian giữa PaymentPanel và PaymentService.
 */
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public List<PaymentItem> findPaymentsByOrderId(Long orderId) {
        return paymentService.findPaymentsByOrderId(orderId);
    }

    public void addPayment(PaymentRequest request) {
        paymentService.addPayment(request);
    }
}
