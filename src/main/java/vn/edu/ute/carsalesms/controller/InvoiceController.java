package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.InvoiceItem;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.InvoiceService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * InvoiceController xử lý các yêu cầu liên quan đến hóa đơn.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic hóa đơn.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào các giao diện
 * (InvoiceService, AuditLogService) thay vì các triển khai cụ thể.
 */
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final AuditLogService auditLogService;

    /**
     * Xây dựng một InvoiceController mới với InvoiceService đã cho.
     * @param invoiceService dịch vụ sẽ được sử dụng để quản lý hóa đơn.
     */
    public InvoiceController(InvoiceService invoiceService) {
        this(invoiceService, new NoOpAuditLogService());
    }

    /**
     * Xây dựng một InvoiceController mới với InvoiceService và AuditLogService đã cho.
     * @param invoiceService dịch vụ sẽ được sử dụng để quản lý hóa đơn.
     * @param auditLogService dịch vụ sẽ được sử dụng để ghi lại các hành động.
     */
    public InvoiceController(InvoiceService invoiceService, AuditLogService auditLogService) {
        this.invoiceService = Objects.requireNonNull(invoiceService, "invoiceService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    /**
     * Tìm tất cả các hóa đơn khớp với từ khóa đã cho.
     * @param keyword từ khóa để tìm kiếm.
     * @return danh sách các mục hóa đơn.
     */
    public List<InvoiceItem> findAllInvoices(String keyword) {
        return invoiceService.findAllInvoices(keyword);
    }

    /**
     * Xuất hóa đơn đã cho sang tệp PDF.
     * @param invoiceId ID của hóa đơn cần xuất.
     * @param outputPath đường dẫn đến tệp PDF đầu ra.
     */
    public void exportInvoicePdf(Long invoiceId, Path outputPath) {
        invoiceService.exportInvoicePdf(invoiceId, outputPath);
        auditLogService.log("EXPORT_PDF", "INVOICE", invoiceId, null, "outputPath=" + outputPath);
    }
}
