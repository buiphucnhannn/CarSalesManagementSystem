package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp Entity, đại diện cho bảng `invoices` trong cơ sở dữ liệu.
 * Lưu trữ thông tin của một hóa đơn được phát hành cho một đơn hàng.
 */
@Entity
@Table(name = "invoices")
public class Invoice {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã hóa đơn, là một định danh duy nhất, không được null.
     */
    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    /**
     * Mối quan hệ Một-Một (One-to-One) với thực thể SaleOrder.
     * Mỗi hóa đơn được gắn với một và chỉ một đơn hàng.
     * `unique = true` trên `JoinColumn` củng cố thêm ràng buộc 1-1 ở mức cơ sở dữ liệu.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false, unique = true)
    private SaleOrder saleOrder;

    /**
     * Ngày phát hành hóa đơn.
     */
    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    /**
     * Trạng thái của hóa đơn (ví dụ: PENDING, PAID, CANCELLED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 30)
    private InvoiceStatus invoiceStatus = InvoiceStatus.PENDING;

    /**
     * Số tiền thuế (VAT) của hóa đơn.
     */
    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /**
     * Tổng số tiền cuối cùng của hóa đơn (đã bao gồm thuế).
     */
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Ghi chú cho hóa đơn.
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Thời điểm bản ghi được tạo.
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors, Getters, and Setters...
    public Invoice() {
    }

    public Invoice(String invoiceCode, SaleOrder saleOrder, LocalDateTime issuedDate, InvoiceStatus invoiceStatus,
                   BigDecimal taxAmount, BigDecimal totalAmount, String note) {
        this.invoiceCode = invoiceCode;
        this.saleOrder = saleOrder;
        this.issuedDate = issuedDate;
        this.invoiceStatus = invoiceStatus;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.note = note;
    }

    /**
     * Hàm callback của JPA, được gọi tự động trước khi một entity được lưu lần đầu (persist).
     * Dùng để gán giá trị mặc định cho `issuedDate` nếu nó chưa được thiết lập.
     */
    @PrePersist
    public void prePersist() {
        if (issuedDate == null) {
            issuedDate = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public SaleOrder getSaleOrder() {
        return saleOrder;
    }

    public void setSaleOrder(SaleOrder saleOrder) {
        this.saleOrder = saleOrder;
    }

    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDateTime issuedDate) {
        this.issuedDate = issuedDate;
    }

    public InvoiceStatus getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(InvoiceStatus invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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
        return "Invoice{" +
                "id=" + id +
                ", invoiceCode='" + invoiceCode + '\'' +
                ", issuedDate=" + issuedDate +
                ", invoiceStatus=" + invoiceStatus +
                ", taxAmount=" + taxAmount +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
