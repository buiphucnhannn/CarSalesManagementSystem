package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.PaymentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;
import vn.edu.ute.carsalesms.service.PaymentService;

import java.util.List;
import java.util.Objects;

/**
 * PaymentController xử lý các yêu cầu liên quan đến thanh toán.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic thanh toán.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào các giao diện
 * (PaymentService, AuditLogService) thay vì các triển khai cụ thể.
 */
public class PaymentController {

    private final PaymentService paymentService;
    private final AuditLogService auditLogService;

    /**
     * Xây dựng một PaymentController mới với PaymentService đã cho.
     * @param paymentService dịch vụ sẽ được sử dụng để quản lý thanh toán.
     */
    public PaymentController(PaymentService paymentService) {
        this(paymentService, new NoOpAuditLogService());
    }

    /**
     * Xây dựng một PaymentController mới với PaymentService và AuditLogService đã cho.
     * @param paymentService dịch vụ sẽ được sử dụng để quản lý thanh toán.
     * @param auditLogService dịch vụ sẽ được sử dụng để ghi lại các hành động.
     */
    public PaymentController(PaymentService paymentService, AuditLogService auditLogService) {
        this.paymentService = Objects.requireNonNull(paymentService, "paymentService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    /**
     * Tìm tất cả các khoản thanh toán cho một đơn đặt hàng cụ thể.
     * @param orderId ID của đơn đặt hàng để tìm kiếm các khoản thanh toán.
     * @return danh sách các mục thanh toán.
     */
    public List<PaymentItem> findPaymentsByOrderId(Long orderId) {
        return paymentService.findPaymentsByOrderId(orderId);
    }

    /**
     * Thêm một khoản thanh toán mới.
     * @param request yêu cầu thanh toán.
     */
    public void addPayment(PaymentRequest request) {
        paymentService.addPayment(request);
        auditLogService.log("CREATE", "PAYMENT", request.orderId(), null, request.toString());
    }
}
