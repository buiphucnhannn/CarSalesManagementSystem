package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp Entity, đại diện cho bảng `payments` trong cơ sở dữ liệu.
 * Lưu trữ thông tin về một lần thanh toán cho một đơn hàng. Một đơn hàng có thể có nhiều lần thanh toán.
 */
@Entity
@Table(name = "payments")
public class Payment {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã thanh toán, là một định danh duy nhất, không được null.
     */
    @Column(name = "payment_code", nullable = false, unique = true, length = 50)
    private String paymentCode;

    /**
     * Mối quan hệ Nhiều-Một với thực thể SaleOrder.
     * Nhiều lần thanh toán có thể thuộc về cùng một đơn hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false)
    private SaleOrder saleOrder;

    /**
     * Ngày thực hiện thanh toán.
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    /**
     * Số tiền của lần thanh toán này.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * Phương thức thanh toán (ví dụ: CASH, BANK_TRANSFER).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    /**
     * Trạng thái của thanh toán (ví dụ: PENDING, COMPLETED, FAILED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    /**
     * Mã tham chiếu giao dịch, ví dụ mã giao dịch từ ngân hàng.
     */
    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    /**
     * Ghi chú cho lần thanh toán.
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Thời điểm bản ghi được tạo.
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors, Getters, and Setters...
    public Payment() {
    }

    public Payment(String paymentCode, SaleOrder saleOrder, LocalDateTime paymentDate, BigDecimal amount,
                   PaymentMethod paymentMethod, PaymentStatus paymentStatus, String transactionReference, String note) {
        this.paymentCode = paymentCode;
        this.saleOrder = saleOrder;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionReference = transactionReference;
        this.note = note;
    }

    /**
     * Hàm callback của JPA, được gọi tự động trước khi một entity được lưu lần đầu.
     * Dùng để gán giá trị mặc định cho `paymentDate` nếu nó chưa được thiết lập.
     */
    @PrePersist
    public void prePersist() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public SaleOrder getSaleOrder() {
        return saleOrder;
    }

    public void setSaleOrder(SaleOrder saleOrder) {
        this.saleOrder = saleOrder;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", paymentCode='" + paymentCode + '\'' +
                ", paymentDate=" + paymentDate +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
