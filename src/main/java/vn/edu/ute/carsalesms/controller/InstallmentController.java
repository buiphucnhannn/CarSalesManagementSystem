package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.InstallmentItem;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.InstallmentService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * InstallmentController xử lý các yêu cầu liên quan đến trả góp.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic trả góp.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào các giao diện
 * (InstallmentService, AuditLogService) thay vì các triển khai cụ thể.
 */
public class InstallmentController {

    private final InstallmentService installmentService;
    private final AuditLogService auditLogService;

    /**
     * Xây dựng một InstallmentController mới với InstallmentService đã cho.
     * @param installmentService dịch vụ sẽ được sử dụng để quản lý trả góp.
     */
    public InstallmentController(InstallmentService installmentService) {
        this(installmentService, new NoOpAuditLogService());
    }

    /**
     * Xây dựng một InstallmentController mới với InstallmentService và AuditLogService đã cho.
     * @param installmentService dịch vụ sẽ được sử dụng để quản lý trả góp.
     * @param auditLogService dịch vụ sẽ được sử dụng để ghi lại các hành động.
     */
    public InstallmentController(InstallmentService installmentService, AuditLogService auditLogService) {
        this.installmentService = Objects.requireNonNull(installmentService, "installmentService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    /**
     * Tìm tất cả các khoản trả góp cho một đơn đặt hàng cụ thể.
     * @param orderId ID của đơn đặt hàng để tìm kiếm các khoản trả góp.
     * @return danh sách các mục trả góp.
     */
    public List<InstallmentItem> findByOrderId(Long orderId) {
        return installmentService.findByOrderId(orderId);
    }

    /**
     * Thanh toán một khoản trả góp.
     * @param installmentId ID của khoản trả góp cần thanh toán.
     * @param amountPaid số tiền đã thanh toán.
     * @param note ghi chú.
     */
    public void payInstallment(Long installmentId, BigDecimal amountPaid, String note) {
        installmentService.payInstallment(installmentId, amountPaid, note);
        auditLogService.log("PAY", "INSTALLMENT", installmentId, null, "amount=" + amountPaid + ", note=" + note);
    }
}
