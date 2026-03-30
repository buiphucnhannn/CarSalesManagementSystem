package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.PaymentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;

import java.util.List;

public interface PaymentService {

    /**
     * Lấy lịch sử thanh toán của một đơn bán.
     */
    List<PaymentItem> findPaymentsByOrderId(Long orderId);

    /**
     * Ghi nhận thanh toán mới cho một đơn bán.
     * Cập nhật tự động OrderStatus sang PAID nếu thanh toán đủ.
     * Sinh tự động Hóa đơn (Invoice) nếu hoàn tất thanh toán.
     */
    void addPayment(PaymentRequest request);
}
