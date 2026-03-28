package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng sale_orders.
 * Lưu thông tin tổng quan của một đơn bán hàng.
 */
@Entity
@Table(name = "sale_orders")
public class SaleOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    /**
     * Quan hệ nhiều - một:
     * Nhiều đơn bán có thể thuộc cùng một khách hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Quan hệ nhiều - một:
     * Nhiều đơn bán có thể do cùng một nhân viên tạo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    /**
     * Quan hệ nhiều - một:
     * Nhiều đơn bán có thể áp dụng cùng một chương trình khuyến mãi.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Quan hệ một - nhiều:
     * Một đơn bán có thể có nhiều dòng chi tiết.
     */
    @OneToMany(mappedBy = "saleOrder", fetch = FetchType.LAZY)
    private List<SaleOrderDetail> saleOrderDetails = new ArrayList<>();

    /**
     * Quan hệ một - nhiều:
     * Một đơn bán có thể phát sinh nhiều lần thanh toán.
     */
    @OneToMany(mappedBy = "saleOrder", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    /**
     * Quan hệ một - một:
     * Mỗi đơn bán có tối đa một hóa đơn.
     */
    @OneToOne(mappedBy = "saleOrder", fetch = FetchType.LAZY)
    private Invoice invoice;

    /**
     * Quan hệ một - nhiều:
     * Một đơn bán trả góp có thể có nhiều kỳ thanh toán.
     */
    @OneToMany(mappedBy = "saleOrder", fetch = FetchType.LAZY)
    private List<InstallmentPlan> installmentPlans = new ArrayList<>();

    public SaleOrder() {
    }

    public SaleOrder(String orderCode, Customer customer, Staff staff, Promotion promotion, LocalDateTime orderDate,
                     BigDecimal totalAmount, BigDecimal discountAmount, BigDecimal finalAmount,
                     PaymentMethod paymentMethod, OrderStatus orderStatus, String note) {
        this.orderCode = orderCode;
        this.customer = customer;
        this.staff = staff;
        this.promotion = promotion;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.orderStatus = orderStatus;
        this.note = note;
    }

    @PrePersist
    public void prePersist() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
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

    public List<SaleOrderDetail> getSaleOrderDetails() {
        return saleOrderDetails;
    }

    public void setSaleOrderDetails(List<SaleOrderDetail> saleOrderDetails) {
        this.saleOrderDetails = saleOrderDetails;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public List<InstallmentPlan> getInstallmentPlans() {
        return installmentPlans;
    }

    public void setInstallmentPlans(List<InstallmentPlan> installmentPlans) {
        this.installmentPlans = installmentPlans;
    }

    @Override
    public String toString() {
        return "SaleOrder{" +
                "id=" + id +
                ", orderCode='" + orderCode + '\'' +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", discountAmount=" + discountAmount +
                ", finalAmount=" + finalAmount +
                ", paymentMethod=" + paymentMethod +
                ", orderStatus=" + orderStatus +
                '}';
    }
}