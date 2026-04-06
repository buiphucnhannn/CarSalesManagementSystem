package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Lớp Entity, đại diện cho bảng `sale_order_details` trong cơ sở dữ liệu.
 * Bảng này hoạt động như một bảng trung gian, lưu trữ thông tin chi tiết về từng sản phẩm (xe) trong một đơn hàng.
 */
@Entity
@Table(name = "sale_order_details")
public class SaleOrderDetail {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mối quan hệ Nhiều-Một với thực thể SaleOrder.
     * Nhiều dòng chi tiết có thể thuộc về cùng một đơn hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false)
    private SaleOrder saleOrder;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Car.
     * Nhiều dòng chi tiết (trong các đơn hàng khác nhau) có thể cùng tham chiếu đến một mẫu xe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    /**
     * Số lượng xe được mua trong dòng chi tiết này.
     */
    @Column(nullable = false)
    private Integer quantity = 1;

    /**
     * Đơn giá của xe tại thời điểm mua.
     */
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /**
     * Số tiền giảm giá cho riêng dòng này (nếu có).
     */
    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * Tổng thành tiền của dòng này (số lượng * đơn giá - giảm giá).
     */
    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    /**
     * Ghi chú cho dòng chi tiết này.
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Mối quan hệ Một-Một với thực thể Warranty.
     * Mỗi dòng chi tiết đơn hàng (tương ứng một chiếc xe bán ra) có thể phát sinh một phiếu bảo hành.
     */
    @OneToOne(mappedBy = "saleOrderDetail", fetch = FetchType.LAZY)
    private Warranty warranty;

    // Constructors, Getters, and Setters...
    public SaleOrderDetail() {
    }

    public SaleOrderDetail(SaleOrder saleOrder, Car car, Integer quantity, BigDecimal unitPrice,
                           BigDecimal discountAmount, BigDecimal lineTotal, String note) {
        this.saleOrder = saleOrder;
        this.car = car;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountAmount = discountAmount;
        this.lineTotal = lineTotal;
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

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Warranty getWarranty() {
        return warranty;
    }

    public void setWarranty(Warranty warranty) {
        this.warranty = warranty;
    }

    @Override
    public String toString() {
        return "SaleOrderDetail{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", discountAmount=" + discountAmount +
                ", lineTotal=" + lineTotal +
                '}';
    }
}
