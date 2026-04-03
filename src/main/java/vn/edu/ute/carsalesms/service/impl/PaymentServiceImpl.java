package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.PaymentDao;
import vn.edu.ute.carsalesms.dao.SaleOrderDao;
import vn.edu.ute.carsalesms.model.dto.PaymentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.Payment;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.service.PaymentRecordFactory;
import vn.edu.ute.carsalesms.service.PaymentInstallmentPlanService;
import vn.edu.ute.carsalesms.service.PaymentOrderFinalizationService;
import vn.edu.ute.carsalesms.service.PaymentService;
import vn.edu.ute.carsalesms.service.PaymentValidationService;
import vn.edu.ute.carsalesms.session.UserSessionContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Cài đặt PaymentService: xử lý logic thanh toán đơn bán và sinh Hóa Đơn.
 */
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;
    private final SaleOrderDao orderDao;
    private final UserSessionContext sessionContext;
    private final PaymentValidationService paymentValidationService;
    private final PaymentInstallmentPlanService paymentInstallmentPlanService;
    private final PaymentOrderFinalizationService paymentOrderFinalizationService;
    private final PaymentRecordFactory paymentRecordFactory;


    public PaymentServiceImpl(PaymentDao paymentDao,
                              SaleOrderDao orderDao,
                              UserSessionContext sessionContext,
                              PaymentValidationService paymentValidationService,
                              PaymentInstallmentPlanService paymentInstallmentPlanService,
                              PaymentOrderFinalizationService paymentOrderFinalizationService,
                              PaymentRecordFactory paymentRecordFactory) {
        this.paymentDao = Objects.requireNonNull(paymentDao, "paymentDao is required");
        this.orderDao = Objects.requireNonNull(orderDao, "orderDao is required");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext is required");
        this.paymentValidationService = Objects.requireNonNull(paymentValidationService, "paymentValidationService is required");
        this.paymentInstallmentPlanService = Objects.requireNonNull(paymentInstallmentPlanService, "paymentInstallmentPlanService is required");
        this.paymentOrderFinalizationService = Objects.requireNonNull(paymentOrderFinalizationService, "paymentOrderFinalizationService is required");
        this.paymentRecordFactory = Objects.requireNonNull(paymentRecordFactory, "paymentRecordFactory is required");
    }

    @Override
    public List<PaymentItem> findPaymentsByOrderId(Long orderId) {
        SaleOrder order = orderDao.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn bán không tồn tại."));
        assertOrderAccess(order);
        List<Payment> list = paymentDao.findByOrderId(orderId);
        return list.stream().map(this::mapToItem).toList();
    }

    @Override
    public void addPayment(PaymentRequest request) {
        SaleOrder order = orderDao.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Đơn bán không tồn tại."));
        assertOrderAccess(order);

        BigDecimal totalPaidSoFar = paymentDao.sumCompletedByOrderId(order.getId());
        paymentValidationService.validate(request, order, totalPaidSoFar);

        Payment pm = paymentRecordFactory.create(order, request);
        paymentDao.save(pm); // Lưu thanh toán

        // Sau khi lưu, tính tổng đã thanh toán
        BigDecimal totalPaid = paymentDao.sumCompletedByOrderId(order.getId());

        paymentInstallmentPlanService.createInitialPlansIfNeeded(order, request, totalPaid);
        paymentOrderFinalizationService.finalizeAfterPayment(order, totalPaid);

        orderDao.save(order);
    }


    private PaymentItem mapToItem(Payment p) {
        return new PaymentItem(
                p.getId(),
                p.getPaymentCode(),
                p.getSaleOrder().getId(),
                p.getSaleOrder().getOrderCode(),
                p.getPaymentDate(),
                p.getAmount(),
                p.getPaymentMethod(),
                p.getPaymentStatus(),
                p.getTransactionReference(),
                p.getNote()
        );
    }


    private void assertOrderAccess(SaleOrder order) {
        Long branchId = order == null || order.getStaff() == null || order.getStaff().getBranch() == null
                ? null
                : order.getStaff().getBranch().getId();
        String branchName = order == null || order.getStaff() == null || order.getStaff().getBranch() == null
                ? null
                : order.getStaff().getBranch().getBranchName();
        sessionContext.assertBranchAccess(branchId, branchName);
    }
}
