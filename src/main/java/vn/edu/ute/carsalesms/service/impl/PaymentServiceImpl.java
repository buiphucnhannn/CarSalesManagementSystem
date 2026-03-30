package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.InstallmentPlanDao;
import vn.edu.ute.carsalesms.dao.InvoiceDao;
import vn.edu.ute.carsalesms.dao.PaymentDao;
import vn.edu.ute.carsalesms.dao.SaleOrderDao;
import vn.edu.ute.carsalesms.model.dto.PaymentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.InstallmentPlan;
import vn.edu.ute.carsalesms.model.entity.Invoice;
import vn.edu.ute.carsalesms.model.entity.Payment;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;
import vn.edu.ute.carsalesms.model.enums.InvoiceStatus;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;
import vn.edu.ute.carsalesms.service.PaymentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Cài đặt PaymentService: xử lý logic thanh toán đơn bán và sinh Hóa Đơn.
 */
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;
    private final SaleOrderDao orderDao;
    private final InvoiceDao invoiceDao;
    private final InstallmentPlanDao installmentPlanDao;

    public PaymentServiceImpl(PaymentDao paymentDao, SaleOrderDao orderDao, InvoiceDao invoiceDao, InstallmentPlanDao installmentPlanDao) {
        this.paymentDao = paymentDao;
        this.orderDao = orderDao;
        this.invoiceDao = invoiceDao;
        this.installmentPlanDao = installmentPlanDao;
    }

    @Override
    public List<PaymentItem> findPaymentsByOrderId(Long orderId) {
        List<Payment> list = paymentDao.findByOrderId(orderId);
        return list.stream().map(this::mapToItem).collect(Collectors.toList());
    }

    @Override
    public void addPayment(PaymentRequest request) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }

        SaleOrder order = orderDao.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Đơn bán không tồn tại."));

        // Nếu thanh toán qua giao diện Trả Góp (Method = INSTALLMENT từ InstallmentService dội sang) 
        // thì bỏ qua kiểm tra PAID (Chữa cháy cho Seed Data cũ thường hay set Order là PAID dù còn dư nợ).
        if (order.getOrderStatus() == OrderStatus.PAID && request.paymentMethod() != PaymentMethod.INSTALLMENT) {
            throw new IllegalStateException("Đơn đã thanh toán đầy đủ, không thể tạo thanh toán mới.");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn đã bị hủy.");
        }

        // Tạo Entity Payment mới
        Payment pm = new Payment();
        pm.setPaymentCode(generateCode("PM-"));
        pm.setSaleOrder(order);
        pm.setPaymentDate(LocalDateTime.now());
        pm.setAmount(request.amount());
        pm.setPaymentMethod(request.paymentMethod());
        pm.setPaymentStatus(PaymentStatus.COMPLETED); // Tạm coi giao dịch thành công ngay lập tức
        pm.setTransactionReference(request.transactionReference());
        pm.setNote(request.note());

        paymentDao.save(pm); // Lưu thanh toán

        // Sau khi lưu, tính tổng đã thanh toán
        BigDecimal totalPaid = paymentDao.sumCompletedByOrderId(order.getId());
        
        // Kiểm tra logic Trả góp: Lần đầu tiên thanh toán nếu là INSTALLMENT thì tạo Plan
        if (request.paymentMethod() == PaymentMethod.INSTALLMENT && 
            request.installmentMonths() != null && request.installmentMonths() > 0) {
            
            // Tìm thử xem đã có Plan nào chưa
            List<InstallmentPlan> existingPlans = installmentPlanDao.findByOrderId(order.getId());
            if (existingPlans.isEmpty()) {
                BigDecimal remaining = order.getFinalAmount().subtract(totalPaid);
                if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                    // Cập nhật trạng thái đơn thành CONFIRMED thay vì PENDING
                    order.setOrderStatus(OrderStatus.CONFIRMED);

                    int months = request.installmentMonths();
                    BigDecimal perMonthAmount = remaining.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
                    
                    // Tạo danh sách Plan bằng Stream API
                    List<InstallmentPlan> newPlans = IntStream.rangeClosed(1, months)
                            .mapToObj(i -> {
                                BigDecimal dueAmount = (i == months) ? 
                                    remaining.subtract(perMonthAmount.multiply(BigDecimal.valueOf(months - 1))) : perMonthAmount;
                                
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
                            .collect(Collectors.toList());
                    
                    installmentPlanDao.saveAll(newPlans);
                }
            }
        }

        // Cập nhật trạng thái PAID nếu đã đủ
        if (totalPaid.compareTo(order.getFinalAmount()) >= 0) {
            order.setOrderStatus(OrderStatus.PAID);
            createInvoiceIfAbsent(order);
        } else if (order.getOrderStatus() == OrderStatus.PENDING && totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }
        
        orderDao.save(order);
    }

    /**
     * Tự động sinh Hóa đơn khi đạt trạng thái PAID.
     */
    private void createInvoiceIfAbsent(SaleOrder order) {
        if (invoiceDao.findByOrderId(order.getId()).isPresent()) {
            return; // Đã sinh hóa đơn
        }

        // Tính thuế (ví dụ mặc định VAT 10% như mô tả trong plan)
        BigDecimal tenPercent = new BigDecimal("0.10");
        BigDecimal taxAmount = order.getFinalAmount().multiply(tenPercent);
        BigDecimal totalWithTax = order.getFinalAmount().add(taxAmount);

        Invoice inv = new Invoice();
        inv.setInvoiceCode(generateCode("INV-"));
        inv.setSaleOrder(order);
        inv.setIssuedDate(LocalDateTime.now());
        inv.setInvoiceStatus(InvoiceStatus.ISSUED);
        inv.setTaxAmount(taxAmount);
        inv.setTotalAmount(totalWithTax);
        inv.setNote("Tự động sinh khi thanh toán đủ. Đơn: " + order.getOrderCode());

        invoiceDao.save(inv);
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

    /** Helper sinh mã bằng UUID cắt ngắn. */
    private String generateCode(String prefix) {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + uuidPart;
    }
}
