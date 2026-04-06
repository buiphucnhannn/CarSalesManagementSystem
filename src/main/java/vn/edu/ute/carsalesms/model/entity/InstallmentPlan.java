package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lớp Entity, đại diện cho bảng `installment_plans` trong cơ sở dữ liệu.
 * Mỗi đối tượng của lớp này tương ứng với một kỳ hạn thanh toán trong một kế hoạch trả góp của một đơn hàng.
 */
@Entity
@Table(
        name = "installment_plans",
        // Thêm một ràng buộc duy nhất ở mức bảng để đảm bảo rằng trong cùng một đơn hàng,
        // không thể có hai kỳ hạn có cùng số thứ tự (installment_no).
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_installment_plans_order_no", columnNames = {"sale_order_id", "installment_no"})
        }
)
public class InstallmentPlan {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mối quan hệ Nhiều-Một với thực thể SaleOrder.
     * Nhiều kỳ hạn thanh toán có thể thuộc về cùng một đơn hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false)
    private SaleOrder saleOrder;

    /**
     * Số thứ tự của kỳ hạn thanh toán (ví dụ: kỳ 1, kỳ 2, ...).
     */
    @Column(name = "installment_no", nullable = false)
    private Integer installmentNo;

    /**
     * Ngày đến hạn thanh toán cho kỳ này.
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Số tiền phải trả cho kỳ hạn này.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * Số tiền đã thực trả cho kỳ hạn này.
     */
    @Column(name = "paid_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Trạng thái của kỳ hạn (ví dụ: UNPAID - chưa trả, PAID - đã trả, OVERDUE - quá hạn).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "installment_status", nullable = false, length = 30)
    private InstallmentStatus installmentStatus = InstallmentStatus.UNPAID;

    /**
     * Ghi chú cho kỳ hạn thanh toán.
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Thời điểm bản ghi được tạo.
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời điểm bản ghi được cập nhật lần cuối.
     */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Constructors, Getters, and Setters...
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
