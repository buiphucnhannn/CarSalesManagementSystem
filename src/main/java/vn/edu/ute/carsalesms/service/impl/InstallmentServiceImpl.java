package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.InstallmentPlanDao;
import vn.edu.ute.carsalesms.model.dto.InstallmentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.InstallmentPlan;
import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.service.InstallmentService;
import vn.edu.ute.carsalesms.service.PaymentService;
import vn.edu.ute.carsalesms.session.CurrentSessionContextAdapter;
import vn.edu.ute.carsalesms.session.UserSessionContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InstallmentServiceImpl implements InstallmentService {

    private final InstallmentPlanDao installmentPlanDao;
    private final PaymentService paymentService;
    private final UserSessionContext sessionContext;

    public InstallmentServiceImpl(InstallmentPlanDao installmentPlanDao, PaymentService paymentService) {
        this(installmentPlanDao, paymentService, new CurrentSessionContextAdapter());
    }

    public InstallmentServiceImpl(InstallmentPlanDao installmentPlanDao,
                                  PaymentService paymentService,
                                  UserSessionContext sessionContext) {
        this.installmentPlanDao = Objects.requireNonNull(installmentPlanDao, "installmentPlanDao is required");
        this.paymentService = Objects.requireNonNull(paymentService, "paymentService is required");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext is required");
    }

    @Override
    public List<InstallmentItem> findByOrderId(Long orderId) {
        List<InstallmentPlan> plans = installmentPlanDao.findByOrderId(orderId);
        if (!plans.isEmpty()) {
            assertPlanAccess(plans.get(0));
        }
        
        // TỰ ĐỘNG CHỮA LÀNH KẾT TỦA DỮ LIỆU (Auto-Heal)
        // Những data cũ trên CSDL bị lưu lộn Status UNPAID dù Đã đóng đủ tiền.
        for (InstallmentPlan plan : plans) {
            BigDecimal delta = plan.getAmount().subtract(plan.getPaidAmount());
            if (delta.compareTo(new BigDecimal("1000")) <= 0 && plan.getInstallmentStatus() == InstallmentStatus.UNPAID) {
                plan.setPaidAmount(plan.getAmount()); // Bơm full tràn số thập phân
                plan.setInstallmentStatus(InstallmentStatus.PAID);
                installmentPlanDao.update(plan); // Commit lại CSDL
            }
        }

        return plans.stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    @Override
    public void payInstallment(Long installmentId, BigDecimal amountPaid, String note) {
        if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền trả không hợp lệ");
        }

        InstallmentPlan currentPlan = installmentPlanDao.findById(installmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỳ hạn"));
        assertPlanAccess(currentPlan);

        if (currentPlan.getInstallmentStatus() == InstallmentStatus.PAID) {
            throw new IllegalStateException("Kỳ hạn này đã được thanh toán đủ.");
        }

        List<InstallmentPlan> allPlans = installmentPlanDao.findByOrderId(currentPlan.getSaleOrder().getId());

        // CHẶN NHẢY CÓC CÁC KỲ TRƯỚC (Ràng buộc nghiệp vụ Số 1)
        boolean hasUnpaidPrev = allPlans.stream()
                .anyMatch(p -> p.getInstallmentNo() < currentPlan.getInstallmentNo() 
                               && p.getInstallmentStatus() != InstallmentStatus.PAID);
        if (hasUnpaidPrev) {
            throw new IllegalStateException("LỖI TÀI CHÍNH: Lịch trả góp bị ngắt quãng! Yêu cầu thanh toán Dứt Điểm nợ gốc của toàn bộ Các Kỳ Hạn Trước đó (Số kỳ nhỏ hơn " + currentPlan.getInstallmentNo() + ")! Lưu ý lỗi Số dư lẻ (Dưới 1 ngàn đồng) sẽ làm Giao dịch bị liệt vào UNPAID.");
        }

        // 1. Tính nợ chuẩn của kỳ này
        BigDecimal currentDebt = currentPlan.getAmount().subtract(currentPlan.getPaidAmount());
        if (currentDebt.compareTo(BigDecimal.ZERO) < 0) currentDebt = BigDecimal.ZERO;

        BigDecimal excessAmount = BigDecimal.ZERO;
        BigDecimal latePenaltyFee = BigDecimal.ZERO;
        BigDecimal wallet = amountPaid;
        String transactionNote = "Thanh toán cho kỳ số " + currentPlan.getInstallmentNo() + ". ";

        // KIỂM TRA TRỄ HẠN (Ràng buộc nghiệp vụ Số 2) - Phạt 15% dư nợ
        if (java.time.LocalDate.now().isAfter(currentPlan.getDueDate())) {
            latePenaltyFee = currentDebt.multiply(new BigDecimal("0.15")); // Phạt 15%
            
            if (wallet.compareTo(latePenaltyFee) <= 0) {
                // Khách mang tiền không đủ trả Phí phạt: Tiền bị nuốt trọn vào Tiền Khôn. Nợ gốc ko giảm 1 ĐỒNG.
                transactionNote += String.format("Trễ Hạn! Bị thu phạt 15%%: %,.0f đ. Số tiền nộp (%,.0f đ) CHỈ ĐỦ HOẶC THIẾU để Lấp Phạt Trễ. Nợ Gốc Không Thay Đổi!", latePenaltyFee, amountPaid);
                wallet = BigDecimal.ZERO; // Sạch túi
            } else {
                // Khách đủ trả phạt và còn Dư tiền lấp Nợ Gốc
                wallet = wallet.subtract(latePenaltyFee);
                transactionNote += String.format("Trễ Hạn! Phạt 15%%: %,.0f đ. Tiền còn để Gán Nợ (Sau phạt): %,.0f đ. ", latePenaltyFee, wallet);
            }
        }

        BigDecimal earlyPenaltyFee = BigDecimal.ZERO; // Đổi biến cho khỏi nhầm fee
        BigDecimal amountToCurrentPlan = wallet;

        // 2. Phân loại Khách đóng lố hay đóng đủ
        if (wallet.compareTo(currentDebt) > 0) {
            // ---> ĐÓNG LỐ <---
            amountToCurrentPlan = currentDebt;
            excessAmount = wallet.subtract(currentDebt);
            
            // Tính phí phạt sớm 10% trên số dư nộp
            earlyPenaltyFee = excessAmount.multiply(new BigDecimal("0.10"));
            
            // Lượng Dư đem đi cấn trừ lùi
            BigDecimal amountToNextPlans = excessAmount.subtract(earlyPenaltyFee);
            transactionNote += String.format(" Thu dư: %,.0f đ. Phí Phạt Sớm (10%%): %,.0f đ. Đem cấn trừ nợ tương lai: %,.0f đ. ", excessAmount, earlyPenaltyFee, amountToNextPlans);

            // Cập nhật lấp đầy cho Plan hiện tại
            currentPlan.setPaidAmount(currentPlan.getAmount());
            currentPlan.setInstallmentStatus(InstallmentStatus.PAID);
            if (note != null && !note.trim().isEmpty()) currentPlan.setNote(note);
            installmentPlanDao.update(currentPlan);

            // 3. Vòng lặp xối xả đắp tiền dư cho Các Kỳ Tương Lai
            if (amountToNextPlans.compareTo(BigDecimal.ZERO) > 0) {
                 // Lọc các kỳ hạn chưa đóng full và có số Kỳ LỚN HƠN số kỳ hiện tại, Sắp xếp Tăng dần theo thời gian/kế hoạch
                 List<InstallmentPlan> futurePlans = allPlans.stream()
                         .filter(p -> p.getInstallmentStatus() != InstallmentStatus.PAID && p.getInstallmentNo() > currentPlan.getInstallmentNo())
                         .sorted((p1, p2) -> Integer.compare(p1.getInstallmentNo(), p2.getInstallmentNo()))
                         .collect(Collectors.toList());

                 for (InstallmentPlan fPlan : futurePlans) {
                     if (amountToNextPlans.compareTo(BigDecimal.ZERO) <= 0) break; // Hết tiền để lấp

                     BigDecimal fDebt = fPlan.getAmount().subtract(fPlan.getPaidAmount());
                     if (fDebt.compareTo(BigDecimal.ZERO) <= 0) continue;

                     BigDecimal absorb = amountToNextPlans;
                     if (absorb.compareTo(fDebt) >= 0) {
                         // Lấp LUÔN kỳ tương lai này cho đầy
                         fPlan.setPaidAmount(fPlan.getAmount());
                         fPlan.setInstallmentStatus(InstallmentStatus.PAID);
                         fPlan.setNote("Được cấn trừ nợ (Xóa Sổ) từ tiền đóng vượt ở Kỳ " + currentPlan.getInstallmentNo());
                         amountToNextPlans = amountToNextPlans.subtract(fDebt);
                     } else {
                         // Mới lấp được một phần nợ
                         fPlan.setPaidAmount(fPlan.getPaidAmount().add(absorb));
                         fPlan.setNote(String.format("Được cấn trừ 1 phần nợ (%,.0f đ) từ Kỳ %d", absorb, currentPlan.getInstallmentNo()));
                         amountToNextPlans = BigDecimal.ZERO; 
                     }
                     installmentPlanDao.update(fPlan); // Lưu lại vào Database
                 } // End future plan looping
            }
        } else {
            // ---> ĐÓNG VỪA ĐỦ HOẶC THIẾU <--- (Không tính phạt Phí)
            BigDecimal newPaid = currentPlan.getPaidAmount().add(amountToCurrentPlan);
            currentPlan.setPaidAmount(newPaid);
            
            // Xóa Sổ Chênh Lệch Lẻ do chia thập phân (<= 1000 vnd)
            BigDecimal delta = currentPlan.getAmount().subtract(newPaid);
            if (delta.compareTo(new BigDecimal("1000")) <= 0) {
                // Tự động Bơm Đầy luôn để tránh Khách vướng Thập phân 0.33 và bị giam UNPAID
                currentPlan.setPaidAmount(currentPlan.getAmount());
                currentPlan.setInstallmentStatus(InstallmentStatus.PAID);
            } else {
                currentPlan.setInstallmentStatus(InstallmentStatus.UNPAID);
            }
            if (note != null && !note.trim().isEmpty()) currentPlan.setNote(note);
            installmentPlanDao.update(currentPlan);
        }

        // 4. Phát sinh thanh toán cho Order tổng (Mặc định payment ghi lại tổng tiền Vốn/Lãi mà khách trực tiếp nộp cho Shop)
        PaymentRequest req = new PaymentRequest(
                currentPlan.getSaleOrder().getId(),
                amountPaid,
                PaymentMethod.INSTALLMENT, 
                "Kỳ " + currentPlan.getInstallmentNo(),
                transactionNote,
                null 
        );

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

    private void assertPlanAccess(InstallmentPlan plan) {
        Long branchId = plan == null
                || plan.getSaleOrder() == null
                || plan.getSaleOrder().getStaff() == null
                || plan.getSaleOrder().getStaff().getBranch() == null
                ? null
                : plan.getSaleOrder().getStaff().getBranch().getId();
        String branchName = plan == null
                || plan.getSaleOrder() == null
                || plan.getSaleOrder().getStaff() == null
                || plan.getSaleOrder().getStaff().getBranch() == null
                ? null
                : plan.getSaleOrder().getStaff().getBranch().getBranchName();
        sessionContext.assertBranchAccess(branchId, branchName);
    }
}
