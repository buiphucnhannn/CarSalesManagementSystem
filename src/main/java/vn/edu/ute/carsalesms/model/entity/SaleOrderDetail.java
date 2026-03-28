package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entity đại diện cho bảng sale_order_details.
 * Lưu từng dòng chi tiết xe thuộc một đơn bán.
 */
@Entity
@Table(name = "sale_order_details")
public class SaleOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quan hệ nhiều - một:
     * Nhiều dòng chi tiết có thể thuộc cùng một đơn bán.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false)
    private SaleOrder saleOrder;

    /**
     * Quan hệ nhiều - một:
     * Nhiều dòng chi tiết có thể cùng tham chiếu tới một xe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Quan hệ một - một:
     * Một dòng chi tiết đơn hàng có thể phát sinh một bảo hành.
     */
    @OneToOne(mappedBy = "saleOrderDetail", fetch = FetchType.LAZY)
    private Warranty warranty;

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