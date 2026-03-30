package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.InstallmentPlanDao;
import vn.edu.ute.carsalesms.model.dto.InstallmentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.InstallmentPlan;
import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.service.InstallmentService;
import vn.edu.ute.carsalesms.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class InstallmentServiceImpl implements InstallmentService {

    private final InstallmentPlanDao installmentPlanDao;
    private final PaymentService paymentService;

    public InstallmentServiceImpl(InstallmentPlanDao installmentPlanDao, PaymentService paymentService) {
        this.installmentPlanDao = installmentPlanDao;
        this.paymentService = paymentService;
    }

    @Override
    public List<InstallmentItem> findByOrderId(Long orderId) {
        return installmentPlanDao.findByOrderId(orderId)
                .stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    @Override
    public void payInstallment(Long installmentId, BigDecimal amountPaid, String note) {
        if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền trả không hợp lệ");
        }

        InstallmentPlan plan = installmentPlanDao.findById(installmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỳ hạn"));

        if (plan.getInstallmentStatus() == InstallmentStatus.PAID) {
            throw new IllegalStateException("Kỳ hạn này đã được thanh toán đủ.");
        }

        // Cập nhật paidAmount
        BigDecimal newPaidAmount = plan.getPaidAmount().add(amountPaid);
        plan.setPaidAmount(newPaidAmount);

        // Kiểm tra xem đã trả đủ kỳ này chưa
        if (newPaidAmount.compareTo(plan.getAmount()) >= 0) {
            plan.setInstallmentStatus(InstallmentStatus.PAID);
        } else {
            plan.setInstallmentStatus(InstallmentStatus.UNPAID);
        }
        
        if (note != null && !note.trim().isEmpty()) {
            plan.setNote(note);
        }

        installmentPlanDao.update(plan);

        // Phát sinh thanh toán cho Order tổng
        PaymentRequest req = new PaymentRequest(
                plan.getSaleOrder().getId(),
                amountPaid,
                PaymentMethod.INSTALLMENT, // Xem như nguồn gốc từ Installment
                "Kỳ " + plan.getInstallmentNo(),
                "Thanh toán qua kỳ trả góp số " + plan.getInstallmentNo(),
                null // Vì chỉ là lượt thanh toán thông thường, ko phát sinh đẻ thêm kỳ trả góp mới
        );

        // Hàm này tự xử lý gen hóa đơn nếu như đủ tổng (totalPaid >= finalAmount)
        paymentService.addPayment(req);
    }

    private InstallmentItem mapToItem(InstallmentPlan plan) {
        return new InstallmentItem(
                plan.getId(),
                plan.getSaleOrder().getId(),
                plan.getSaleOrder().getOrderCode(),
                plan.getSaleOrder().getCustomer().getFullName(),
                plan.getInstallmentNo(),
                plan.getDueDate(),
                plan.getAmount(),
                plan.getPaidAmount(),
                plan.getInstallmentStatus(),
                plan.getNote()
        );
    }
}
