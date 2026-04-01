package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.InvoiceDao;
import vn.edu.ute.carsalesms.model.dto.InvoiceItem;
import vn.edu.ute.carsalesms.model.entity.Invoice;
import vn.edu.ute.carsalesms.service.InvoicePdfExporter;
import vn.edu.ute.carsalesms.service.InvoiceService;
import vn.edu.ute.carsalesms.session.CurrentSession;

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
                .filter(this::canAccessInvoice)
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    @Override
    public void exportInvoicePdf(Long invoiceId, Path outputPath) {
        Invoice invoice = invoiceDao.findByIdWithOrderDetails(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dữ liệu hóa đơn để in."));
        assertInvoiceAccess(invoice);

        invoicePdfExporter.export(invoice, outputPath);
    }

    private boolean canAccessInvoice(Invoice invoice) {
        if (CurrentSession.isAdmin()) {
            return true;
        }
        Long sessionBranchId = CurrentSession.currentBranchId();
        if (sessionBranchId == null) {
            return true;
        }
        Long invoiceBranchId = resolveBranchId(invoice);
        return invoiceBranchId != null && sessionBranchId.equals(invoiceBranchId);
    }

    private void assertInvoiceAccess(Invoice invoice) {
        CurrentSession.assertBranchAccess(resolveBranchId(invoice), resolveBranchName(invoice));
    }

    private Long resolveBranchId(Invoice invoice) {
        if (invoice == null
                || invoice.getSaleOrder() == null
                || invoice.getSaleOrder().getStaff() == null
                || invoice.getSaleOrder().getStaff().getBranch() == null) {
            return null;
        }
        return invoice.getSaleOrder().getStaff().getBranch().getId();
    }

    private String resolveBranchName(Invoice invoice) {
        if (invoice == null
                || invoice.getSaleOrder() == null
                || invoice.getSaleOrder().getStaff() == null
                || invoice.getSaleOrder().getStaff().getBranch() == null) {
            return null;
        }
        return invoice.getSaleOrder().getStaff().getBranch().getBranchName();
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
