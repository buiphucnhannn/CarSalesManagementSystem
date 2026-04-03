package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.Payment;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;

/**
 * Tao entity Payment tu request va don hang.
 */
public interface PaymentRecordFactory {

    Payment create(SaleOrder order, PaymentRequest request);
}

