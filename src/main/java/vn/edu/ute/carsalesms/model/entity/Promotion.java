package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Entity, đại diện cho bảng `promotions` trong cơ sở dữ liệu.
 * Lưu trữ thông tin về các chương trình khuyến mãi có thể được áp dụng cho các đơn hàng.
 */
@Entity
@Table(name = "promotions")
public class Promotion {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã khuyến mãi, là một định danh duy nhất, không được null.
     */
    @Column(name = "promotion_code", nullable = false, unique = true, length = 50)
    private String promotionCode;

    /**
     * Tên của chương trình khuyến mãi.
     */
    @Column(name = "promotion_name", nullable = false, length = 255)
    private String promotionName;

    /**
     * Loại giảm giá (ví dụ: "PERCENTAGE" - phần trăm, "FIXED_AMOUNT" - số tiền cố định).
     */
    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType;

    /**
     * Giá trị giảm giá. Có thể là tỷ lệ phần trăm hoặc một số tiền cụ thể.
     */
    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;

    /**
     * Ngày bắt đầu áp dụng chương trình khuyến mãi.
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Ngày kết thúc chương trình khuyến mãi.
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Mô tả chi tiết về chương trình khuyến mãi.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Trạng thái của chương trình khuyến mãi (ví dụ: ACTIVE - đang chạy, INACTIVE - không hoạt động).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

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

    /**
     * Mối quan hệ Một-Nhiều với thực thể SaleOrder.
     * Một chương trình khuyến mãi có thể được áp dụng cho nhiều đơn hàng.
     */
    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    private List<SaleOrder> saleOrders = new ArrayList<>();

    // Constructors, Getters, and Setters...
    public Promotion() {
    }

    public Promotion(String promotionCode, String promotionName, String discountType, BigDecimal discountValue,
                     LocalDate startDate, LocalDate endDate, String description, Status status) {
        this.promotionCode = promotionCode;
        this.promotionName = promotionName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<SaleOrder> getSaleOrders() {
        return saleOrders;
    }

    public void setSaleOrders(List<SaleOrder> saleOrders) {
        this.saleOrders = saleOrders;
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "id=" + id +
                ", promotionCode='" + promotionCode + '\'' +
                ", promotionName='" + promotionName + '\'' +
                ", discountType='" + discountType + '\'' +
                ", discountValue=" + discountValue +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
