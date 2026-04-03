package vn.edu.ute.carsalesms.service;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;

/**
 * Xu ly khoi tao ke hoach tra gop lan dau sau giao dich dat coc.
 */
public interface PaymentInstallmentPlanService {

    void createInitialPlansIfNeeded(SaleOrder order, PaymentRequest request, BigDecimal totalPaid);
}

