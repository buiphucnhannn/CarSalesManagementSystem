package vn.edu.ute.carsalesms.service;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;

/**
 * Xac thuc dieu kien tao giao dich thanh toan.
 */
public interface PaymentValidationService {

    void validate(PaymentRequest request, SaleOrder order, BigDecimal totalPaidSoFar);
}

