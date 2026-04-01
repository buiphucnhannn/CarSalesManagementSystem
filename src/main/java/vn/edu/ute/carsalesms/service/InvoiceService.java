package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.InvoiceItem;

import java.nio.file.Path;
import java.util.List;

public interface InvoiceService {

    /**
     * Tìm tất cả hoá đơn 
     */
    List<InvoiceItem> findAllInvoices(String keyword);

    /**
     * Xuất PDF cho hóa đơn được chọn.
     */
    void exportInvoicePdf(Long invoiceId, Path outputPath);
}
