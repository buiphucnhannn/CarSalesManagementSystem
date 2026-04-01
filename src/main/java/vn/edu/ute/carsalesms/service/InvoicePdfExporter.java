package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.entity.Invoice;

import java.nio.file.Path;

/**
 * Trừu tượng hóa nghiệp vụ xuất hóa đơn PDF (DIP/SOLID).
 */
public interface InvoicePdfExporter {

    void export(Invoice invoice, Path outputPath);
}

