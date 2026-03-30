package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interface DAO cho Payment.
 */
public interface PaymentDao {

    /**
     * Tìm tất cả lần thanh toán của một đơn bán.
     *
     * @param orderId id đơn bán
     * @return danh sách Payment eager-fetch SaleOrder
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Tính tổng số tiền đã thanh toán COMPLETED của một đơn bán.
     *
     * @param orderId id đơn bán
     * @return tổng tiền đã trả, 0 nếu chưa có
     */
    BigDecimal sumCompletedByOrderId(Long orderId);

    /**
     * Tìm Payment theo id.
     */
    Optional<Payment> findById(Long id);

    /**
     * Lưu (thêm mới hoặc cập nhật) Payment.
     */
    Payment save(Payment payment);
}
