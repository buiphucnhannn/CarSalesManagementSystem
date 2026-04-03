package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.entity.SaleOrder;

/**
 * Tu dong kich hoat bao hanh khi don da thanh toan du.
 */
public interface WarrantyAutoActivationService {

	void activateForPaidOrder(SaleOrder order);
}

