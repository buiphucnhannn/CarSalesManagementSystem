package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng invoices.
 * Lưu thông tin hóa đơn của đơn bán.
 */
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    /**
     * Quan hệ một - một:
     * Mỗi hóa đơn gắn với duy nhất một đơn bán.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false, unique = true)
    private SaleOrder saleOrder;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 30)
    private InvoiceStatus invoiceStatus = InvoiceStatus.PENDING;

    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

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