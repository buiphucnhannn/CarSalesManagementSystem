package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.InvoiceDao;
import vn.edu.ute.carsalesms.model.dto.InvoiceItem;
import vn.edu.ute.carsalesms.model.entity.Invoice;
import vn.edu.ute.carsalesms.service.InvoicePdfExporter;
import vn.edu.ute.carsalesms.service.InvoiceService;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceDao;
    private final InvoicePdfExporter invoicePdfExporter;

    public InvoiceServiceImpl(InvoiceDao invoiceDao, InvoicePdfExporter invoicePdfExporter) {
        this.invoiceDao = invoiceDao;
        this.invoicePdfExporter = invoicePdfExporter;
    }

    @Override
    public List<InvoiceItem> findAllInvoices(String keyword) {
        return invoiceDao.findAll(keyword).stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    @Override
    public void exportInvoicePdf(Long invoiceId, Path outputPath) {
        Invoice invoice = invoiceDao.findByIdWithOrderDetails(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dữ liệu hóa đơn để in."));

        invoicePdfExporter.export(invoice, outputPath);
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
