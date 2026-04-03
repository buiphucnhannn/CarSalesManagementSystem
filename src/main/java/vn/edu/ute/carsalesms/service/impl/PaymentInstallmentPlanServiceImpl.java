package vn.edu.ute.carsalesms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import vn.edu.ute.carsalesms.dao.InstallmentPlanDao;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.InstallmentPlan;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.service.PaymentInstallmentPlanService;

public class PaymentInstallmentPlanServiceImpl implements PaymentInstallmentPlanService {

    private final InstallmentPlanDao installmentPlanDao;

    public PaymentInstallmentPlanServiceImpl(InstallmentPlanDao installmentPlanDao) {
        this.installmentPlanDao = Objects.requireNonNull(installmentPlanDao, "installmentPlanDao is required");
    }

    @Override
    public void createInitialPlansIfNeeded(SaleOrder order, PaymentRequest request, BigDecimal totalPaid) {
        if (request.paymentMethod() != PaymentMethod.INSTALLMENT
                || request.installmentMonths() == null
                || request.installmentMonths() <= 0) {
            return;
        }

        List<InstallmentPlan> existingPlans = installmentPlanDao.findByOrderId(order.getId());
        if (!existingPlans.isEmpty()) {
            return;
        }

        BigDecimal remaining = order.getFinalAmount().subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);

        int months = request.installmentMonths();
        BigDecimal perMonthAmount = remaining.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

        List<InstallmentPlan> newPlans = IntStream.rangeClosed(1, months)
                .mapToObj(i -> {
                    BigDecimal dueAmount = (i == months)
                            ? remaining.subtract(perMonthAmount.multiply(BigDecimal.valueOf(months - 1)))
                            : perMonthAmount;

                    return new InstallmentPlan(
                            order,
                            i,
                            LocalDate.now().plusMonths(i),
                            dueAmount,
                            BigDecimal.ZERO,
                            InstallmentStatus.UNPAID,
                            "Tạo tự động"
                    );
                })
                .toList();

        installmentPlanDao.saveAll(newPlans);
    }
}

