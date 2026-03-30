package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.InvoiceDao;
import vn.edu.ute.carsalesms.model.dto.InvoiceItem;
import vn.edu.ute.carsalesms.model.entity.Invoice;
import vn.edu.ute.carsalesms.service.InvoiceService;

import java.util.List;
import java.util.stream.Collectors;

public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceDao;

    public InvoiceServiceImpl(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    @Override
    public List<InvoiceItem> findAllInvoices(String keyword) {
        return invoiceDao.findAll(keyword).stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    private InvoiceItem mapToItem(Invoice i) {
        return new InvoiceItem(
                i.getId(),
                i.getInvoiceCode(),
                i.getSaleOrder() != null ? i.getSaleOrder().getId() : null,
                i.getSaleOrder() != null ? i.getSaleOrder().getOrderCode() : "",
                i.getIssuedDate(),
                i.getTaxAmount(),
                i.getTotalAmount(),
                i.getInvoiceStatus(),
                i.getNote()
        );
    }
}
