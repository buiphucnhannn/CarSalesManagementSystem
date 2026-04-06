package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lớp Entity, đại diện cho bảng `warranties` trong cơ sở dữ liệu.
 * Lưu trữ thông tin về phiếu bảo hành cho một chiếc xe cụ thể được bán ra,
 * phát sinh từ một chi tiết đơn hàng (SaleOrderDetail).
 */
@Entity
@Table(name = "warranties")
public class Warranty {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã bảo hành, là một định danh duy nhất, không được null.
     */
    @Column(name = "warranty_code", nullable = false, unique = true, length = 50)
    private String warrantyCode;

    /**
     * Mối quan hệ Một-Một với thực thể SaleOrderDetail.
     * Mỗi phiếu bảo hành được gắn với một và chỉ một dòng chi tiết đơn hàng (tức là một chiếc xe cụ thể đã bán).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_detail_id", nullable = false, unique = true)
    private SaleOrderDetail saleOrderDetail;

    /**
     * Ngày bắt đầu hiệu lực của bảo hành.
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Ngày kết thúc hiệu lực của bảo hành.
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Trạng thái của phiếu bảo hành (ví dụ: ACTIVE - còn hiệu lực, EXPIRED - hết hạn, VOIDED - bị vô hiệu).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "warranty_status", nullable = false, length = 30)
    private WarrantyStatus warrantyStatus = WarrantyStatus.ACTIVE;

    /**
     * Ghi chú về phiếu bảo hành.
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
