package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng warranties.
 * Lưu thông tin bảo hành phát sinh từ chi tiết đơn hàng.
 */
@Entity
@Table(name = "warranties")
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warranty_code", nullable = false, unique = true, length = 50)
    private String warrantyCode;

    /**
     * Quan hệ một - một:
     * Mỗi bảo hành gắn với duy nhất một dòng chi tiết đơn hàng.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_detail_id", nullable = false, unique = true)
    private SaleOrderDetail saleOrderDetail;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "warranty_status", nullable = false, length = 30)
    private WarrantyStatus warrantyStatus = WarrantyStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Warranty() {
    }

    public Warranty(String warrantyCode, SaleOrderDetail saleOrderDetail, LocalDate startDate,
                    LocalDate endDate, WarrantyStatus warrantyStatus, String note) {
        this.warrantyCode = warrantyCode;
        this.saleOrderDetail = saleOrderDetail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.warrantyStatus = warrantyStatus;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public String getWarrantyCode() {
        return warrantyCode;
    }

    public void setWarrantyCode(String warrantyCode) {
        this.warrantyCode = warrantyCode;
    }

    public SaleOrderDetail getSaleOrderDetail() {
        return saleOrderDetail;
    }

    public void setSaleOrderDetail(SaleOrderDetail saleOrderDetail) {
        this.saleOrderDetail = saleOrderDetail;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public WarrantyStatus getWarrantyStatus() {
        return warrantyStatus;
    }

    public void setWarrantyStatus(WarrantyStatus warrantyStatus) {
        this.warrantyStatus = warrantyStatus;
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
        return "Warranty{" +
                "id=" + id +
                ", warrantyCode='" + warrantyCode + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", warrantyStatus=" + warrantyStatus +
                '}';
    }
}