package vn.edu.ute.carsalesms.service;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;

/**
 * Chot trang thai don hang sau khi ghi nhan thanh toan.
 */
public interface PaymentOrderFinalizationService {

    void finalizeAfterPayment(SaleOrder order, BigDecimal totalPaid);
}

