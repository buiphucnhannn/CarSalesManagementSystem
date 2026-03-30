package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.InstallmentItem;

import java.math.BigDecimal;
import java.util.List;

public interface InstallmentService {

    /** Lấy toàn bộ hợp đồng theo ID Đơn bán */
    List<InstallmentItem> findByOrderId(Long orderId);

    /** 
     * Khách hàng đóng tiền cho một kỳ (Installment) theo đúng ID kỳ.
     * Số tiền đóng vào sẽ trừ trực tiếp vào DueAmount.
     * Tự động sinh thêm 1 Payment vào PaymentPanel để track lại.
     */
    void payInstallment(Long installmentId, BigDecimal amountPaid, String note);
}
