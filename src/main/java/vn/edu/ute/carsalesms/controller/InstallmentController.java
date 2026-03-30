package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.InstallmentItem;
import vn.edu.ute.carsalesms.service.InstallmentService;

import java.math.BigDecimal;
import java.util.List;

public class InstallmentController {

    private final InstallmentService installmentService;

    public InstallmentController(InstallmentService installmentService) {
        this.installmentService = installmentService;
    }

    public List<InstallmentItem> findByOrderId(Long orderId) {
        return installmentService.findByOrderId(orderId);
    }

    public void payInstallment(Long installmentId, BigDecimal amountPaid, String note) {
        installmentService.payInstallment(installmentId, amountPaid, note);
    }
}
