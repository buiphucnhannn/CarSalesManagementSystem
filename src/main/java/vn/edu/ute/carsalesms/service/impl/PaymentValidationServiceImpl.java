package vn.edu.ute.carsalesms.service.impl;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.service.PaymentValidationService;

public class PaymentValidationServiceImpl implements PaymentValidationService {

    @Override
    public void validate(PaymentRequest request, SaleOrder order, BigDecimal totalPaidSoFar) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }

        BigDecimal currentRemaining = order.getFinalAmount().subtract(totalPaidSoFar);

        if (request.paymentMethod() != PaymentMethod.INSTALLMENT && request.amount().compareTo(currentRemaining) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Không hợp lệ! Vượt quá dư nợ tổng đơn (Bạn đang gõ đóng: %,.0f đ, trong khi Đơn chỉ cần trả: %,.0f đ)",
                    request.amount(),
                    currentRemaining
            ));
        }

        if (request.paymentMethod() == PaymentMethod.INSTALLMENT
                && request.installmentMonths() != null
                && request.installmentMonths() > 0
                && request.amount().compareTo(currentRemaining) >= 0) {
            throw new IllegalArgumentException("Khởi tạo Trả Góp lặp lỗi: Số tiền cọc đợt đầu phải BÉ HƠN dư nợ cuối để hệ thống rải đều những tháng còn lại!");
        }

        if (order.getOrderStatus() == OrderStatus.PAID && request.paymentMethod() != PaymentMethod.INSTALLMENT) {
            throw new IllegalStateException("Đơn đã thanh toán đầy đủ, không thể tạo thanh toán mới.");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn đã bị hủy.");
        }
    }
}

