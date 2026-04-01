package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Invoice;

import java.util.Optional;
import java.util.List;

/**
 * Interface DAO cho Invoice.
 */
public interface InvoiceDao {

    /**
     * Tìm hóa đơn theo id đơn bán (quan hệ 1-1).
     *
     * @param orderId id đơn bán
     * @return Optional<Invoice> – empty nếu chưa có hóa đơn
     */
    Optional<Invoice> findByOrderId(Long orderId);

    /**
     * Lưu hóa đơn mới (chỉ tạo, không cập nhật).
     */
    Invoice save(Invoice invoice);

    /**
     * Tải tất cả hóa đơn (hỗ trợ tìm kiếm theo keyword: Mã hóa đơn, khách mua).
     */
    List<Invoice> findAll(String keyword);

    /**
     * Tải hóa đơn theo id và nạp đầy đủ dữ liệu liên quan để xuất PDF.
     */
    Optional<Invoice> findByIdWithOrderDetails(Long invoiceId);
}
