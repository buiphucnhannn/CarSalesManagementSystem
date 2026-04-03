package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.InvoiceItem;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.InvoiceService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class InvoiceController {
    private final InvoiceService invoiceService;
    private final AuditLogService auditLogService;

    public InvoiceController(InvoiceService invoiceService) {
        this(invoiceService, new NoOpAuditLogService());
    }

    public InvoiceController(InvoiceService invoiceService, AuditLogService auditLogService) {
        this.invoiceService = Objects.requireNonNull(invoiceService, "invoiceService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    public List<InvoiceItem> findAllInvoices(String keyword) {
        return invoiceService.findAllInvoices(keyword);
    }

    public void exportInvoicePdf(Long invoiceId, Path outputPath) {
        invoiceService.exportInvoicePdf(invoiceId, outputPath);
        auditLogService.log("EXPORT_PDF", "INVOICE", invoiceId, null, "outputPath=" + outputPath);
    }
}
