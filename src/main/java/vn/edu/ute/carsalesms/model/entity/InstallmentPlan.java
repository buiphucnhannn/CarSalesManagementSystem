package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng installment_plans.
 * Lưu từng kỳ thanh toán trả góp của một đơn bán.
 */
@Entity
@Table(
        name = "installment_plans",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_installment_plans_order_no", columnNames = {"sale_order_id", "installment_no"})
        }
)
public class InstallmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quan hệ nhiều - một:
     * Nhiều kỳ thanh toán có thể thuộc cùng một đơn bán.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false)
    private SaleOrder saleOrder;

    @Column(name = "installment_no", nullable = false)
    private Integer installmentNo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "installment_status", nullable = false, length = 30)
    private InstallmentStatus installmentStatus = InstallmentStatus.UNPAID;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public InstallmentPlan() {
    }

    public InstallmentPlan(SaleOrder saleOrder, Integer installmentNo, LocalDate dueDate, BigDecimal amount,
                           BigDecimal paidAmount, InstallmentStatus installmentStatus, String note) {
        this.saleOrder = saleOrder;
        this.installmentNo = installmentNo;
        this.dueDate = dueDate;
        this.amount = amount;
        this.paidAmount = paidAmount;
        this.installmentStatus = installmentStatus;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public SaleOrder getSaleOrder() {
        return saleOrder;
    }

    public void setSaleOrder(SaleOrder saleOrder) {
        this.saleOrder = saleOrder;
    }

    public Integer getInstallmentNo() {
        return installmentNo;
    }

    public void setInstallmentNo(Integer installmentNo) {
        this.installmentNo = installmentNo;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public InstallmentStatus getInstallmentStatus() {
        return installmentStatus;
    }

    public void setInstallmentStatus(InstallmentStatus installmentStatus) {
        this.installmentStatus = installmentStatus;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "InstallmentPlan{" +
                "id=" + id +
                ", installmentNo=" + installmentNo +
                ", dueDate=" + dueDate +
                ", amount=" + amount +
                ", paidAmount=" + paidAmount +
                ", installmentStatus=" + installmentStatus +
                '}';
    }
}