package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.InstallmentItem;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.InstallmentService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class InstallmentController {

    private final InstallmentService installmentService;
    private final AuditLogService auditLogService;

    public InstallmentController(InstallmentService installmentService) {
        this(installmentService, new NoOpAuditLogService());
    }

    public InstallmentController(InstallmentService installmentService, AuditLogService auditLogService) {
        this.installmentService = Objects.requireNonNull(installmentService, "installmentService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    public List<InstallmentItem> findByOrderId(Long orderId) {
        return installmentService.findByOrderId(orderId);
    }

    public void payInstallment(Long installmentId, BigDecimal amountPaid, String note) {
        installmentService.payInstallment(installmentId, amountPaid, note);
        auditLogService.log("PAY", "INSTALLMENT", installmentId, null, "amount=" + amountPaid + ", note=" + note);
    }
}
