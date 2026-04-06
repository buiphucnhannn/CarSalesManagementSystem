package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Entity, đại diện cho bảng `sale_orders` trong cơ sở dữ liệu.
 * Lưu trữ thông tin tổng quan của một đơn hàng.
 */
@Entity
@Table(name = "sale_orders")
public class SaleOrder {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã đơn hàng, là một định danh duy nhất, không được null.
     */
    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Customer.
     * Nhiều đơn hàng có thể thuộc về cùng một khách hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Staff.
     * Nhiều đơn hàng có thể được tạo bởi cùng một nhân viên.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Promotion.
     * Nhiều đơn hàng có thể áp dụng cùng một chương trình khuyến mãi.
     * `JoinColumn` này có thể null, vì không phải đơn hàng nào cũng có khuyến mãi.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    /**
     * Ngày tạo đơn hàng.
     */
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    /**
     * Tổng giá trị của đơn hàng trước khi áp dụng giảm giá.
     */
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Số tiền được giảm giá từ chương trình khuyến mãi.
     */
    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * Số tiền cuối cùng khách hàng phải trả sau khi đã trừ giảm giá.
     */
    @Column(name = "final_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    /**
     * Phương thức thanh toán chính của đơn hàng.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    /**
     * Trạng thái của đơn hàng (ví dụ: PENDING, CONFIRMED, PAID, CANCELLED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    /**
     * Ghi chú cho đơn hàng.
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

    /**
     * Mối quan hệ Một-Nhiều với thực thể SaleOrderDetail.
     * Một đơn hàng có thể bao gồm nhiều dòng chi tiết (mỗi dòng là một loại xe).
     */
    @OneToMany(mappedBy = "saleOrder", fetch = FetchType.LAZY)
    private List<SaleOrderDetail> saleOrderDetails = new ArrayList<>();

    /**
     * Mối quan hệ Một-Nhiều với thực thể Payment.
     * Một đơn hàng có thể có nhiều lần thanh toán.
     */
    @OneToMany(mappedBy = "saleOrder", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    /**
     * Mối quan hệ Một-Một với thực thể Invoice.
     * Mỗi đơn hàng có thể có một hóa đơn tương ứng.
     */
    @OneToOne(mappedBy = "saleOrder", fetch = FetchType.LAZY)
    private Invoice invoice;

    /**
     * Mối quan hệ Một-Nhiều với thực thể InstallmentPlan.
     * Một đơn hàng trả góp sẽ có nhiều kỳ hạn thanh toán.
     */
    @OneToMany(mappedBy = "saleOrder", fetch = FetchType.LAZY)
    private List<InstallmentPlan> installmentPlans = new ArrayList<>();

    // Constructors, Getters, and Setters...
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

    /**
     * Hàm callback của JPA, tự động gán ngày giờ hiện tại cho `orderDate` khi tạo mới.
     */
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
