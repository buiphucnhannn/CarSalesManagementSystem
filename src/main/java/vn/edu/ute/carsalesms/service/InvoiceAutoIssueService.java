package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.entity.SaleOrder;

/**
 * Tu dong sinh hoa don cho don da thanh toan.
 */
public interface InvoiceAutoIssueService {

    void createIfAbsent(SaleOrder order);
}

