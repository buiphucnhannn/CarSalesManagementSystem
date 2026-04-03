package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.InvoiceDao;
import vn.edu.ute.carsalesms.model.entity.Invoice;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.enums.InvoiceStatus;
import vn.edu.ute.carsalesms.service.InvoiceAutoIssueService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class InvoiceAutoIssueServiceImpl implements InvoiceAutoIssueService {

    private final InvoiceDao invoiceDao;

    public InvoiceAutoIssueServiceImpl(InvoiceDao invoiceDao) {
        this.invoiceDao = Objects.requireNonNull(invoiceDao, "invoiceDao is required");
    }

    @Override
    public void createIfAbsent(SaleOrder order) {
        if (invoiceDao.findByOrderId(order.getId()).isPresent()) {
            return;
        }

        BigDecimal preTaxAmount = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        BigDecimal discountAmount = order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount();
        BigDecimal taxAmount = preTaxAmount.multiply(new BigDecimal("0.10"));
        BigDecimal totalWithTax = preTaxAmount.add(taxAmount).subtract(discountAmount);
        if (totalWithTax.compareTo(BigDecimal.ZERO) < 0) {
            totalWithTax = BigDecimal.ZERO;
        }

        Invoice inv = new Invoice();
        inv.setInvoiceCode(generateCode());
        inv.setSaleOrder(order);
        inv.setIssuedDate(LocalDateTime.now());
        inv.setInvoiceStatus(InvoiceStatus.ISSUED);
        inv.setTaxAmount(taxAmount);
        inv.setTotalAmount(totalWithTax);
        inv.setNote("Tự động sinh khi thanh toán đủ. Đơn: " + order.getOrderCode());

        invoiceDao.save(inv);
    }

    private String generateCode() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

