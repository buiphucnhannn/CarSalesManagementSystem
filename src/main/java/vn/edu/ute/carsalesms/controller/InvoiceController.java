package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.InvoiceItem;
import vn.edu.ute.carsalesms.service.InvoiceService;

import java.nio.file.Path;
import java.util.List;

public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    public List<InvoiceItem> findAllInvoices(String keyword) {
        return invoiceService.findAllInvoices(keyword);
    }

    public void exportInvoicePdf(Long invoiceId, Path outputPath) {
        invoiceService.exportInvoicePdf(invoiceId, outputPath);
    }
}
