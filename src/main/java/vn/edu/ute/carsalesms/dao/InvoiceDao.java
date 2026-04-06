package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Invoice;

import java.util.Optional;
import java.util.List;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Hóa đơn (Invoice).
 * Cung cấp các phương thức để truy cập và thao tác với dữ liệu hóa đơn trong cơ sở dữ liệu.
 */
public interface InvoiceDao {

    /**
     * Tìm kiếm một hóa đơn dựa trên ID của đơn đặt hàng (SaleOrder).
     * Mối quan hệ giữa Hóa đơn và Đơn đặt hàng thường là 1-1.
     *
     * @param orderId ID của đơn đặt hàng.
     * @return Một Optional chứa đối tượng Invoice nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Invoice> findByOrderId(Long orderId);

    /**
     * Lưu một đối tượng hóa đơn mới vào cơ sở dữ liệu.
     * Phương thức này thường được dùng để tạo mới, không dùng để cập nhật.
     *
     * @param invoice Đối tượng Invoice cần lưu.
     * @return Đối tượng Invoice sau khi đã được lưu (có thể chứa ID được tạo tự động).
     */
    Invoice save(Invoice invoice);

    /**
     * Lấy danh sách tất cả các hóa đơn, có hỗ trợ tìm kiếm theo từ khóa.
     *
     * @param keyword Từ khóa để tìm kiếm (ví dụ: mã hóa đơn, tên khách hàng, mã khách hàng).
     * @return Danh sách các đối tượng Invoice phù hợp.
     */
    List<Invoice> findAll(String keyword);

    /**
     * Tìm một hóa đơn dựa trên ID của nó và tải tất cả các dữ liệu liên quan một cách đầy đủ (eager loading).
     * Dữ liệu này bao gồm chi tiết đơn hàng, thông tin khách hàng, v.v., cần thiết cho việc xuất file PDF.
     *
     * @param invoiceId ID của hóa đơn cần tìm.
     * @return Một Optional chứa đối tượng Invoice với đầy đủ thông tin nếu tìm thấy.
     */
    Optional<Invoice> findByIdWithOrderDetails(Long invoiceId);
}
