package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Thanh toán (Payment).
 * Định nghĩa các phương thức cần thiết để tương tác với dữ liệu thanh toán.
 */
public interface PaymentDao {

    /**
     * Tìm tất cả các lần thanh toán liên quan đến một đơn đặt hàng cụ thể.
     *
     * @param orderId ID của đơn đặt hàng (SaleOrder).
     * @return Danh sách các đối tượng Payment, bao gồm cả thông tin SaleOrder được tải sẵn (eager-fetch).
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Tính tổng số tiền đã được thanh toán thành công (trạng thái COMPLETED) cho một đơn đặt hàng.
     *
     * @param orderId ID của đơn đặt hàng.
     * @return Tổng số tiền đã thanh toán. Trả về 0 nếu chưa có thanh toán nào.
     */
    BigDecimal sumCompletedByOrderId(Long orderId);

    /**
     * Tìm một thanh toán dựa trên ID của nó.
     *
     * @param id ID của thanh toán cần tìm.
     * @return Một Optional chứa đối tượng Payment nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Payment> findById(Long id);

    /**
     * Lưu (thêm mới hoặc cập nhật) một bản ghi thanh toán.
     *
     * @param payment Đối tượng Payment cần lưu.
     * @return Đối tượng Payment sau khi đã được lưu.
     */
    Payment save(Payment payment);
}
