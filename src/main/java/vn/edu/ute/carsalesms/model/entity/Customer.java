package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Entity, đại diện cho bảng `customers` trong cơ sở dữ liệu.
 * Lưu trữ thông tin cá nhân của khách hàng.
 * Trong hệ thống này, khách hàng là đối tượng được quản lý bởi nhân viên và không có chức năng tự đăng nhập.
 */
@Entity
@Table(name = "customers")
public class Customer {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã khách hàng, là một định danh duy nhất, không được null.
     */
    @Column(name = "customer_code", nullable = false, unique = true, length = 50)
    private String customerCode;

    /**
     * Tên đầy đủ của khách hàng.
     */
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    /**
     * Số điện thoại của khách hàng, không được null.
     */
    @Column(nullable = false, length = 20)
    private String phone;

    /**
     * Địa chỉ email của khách hàng.
     */
    @Column(length = 100)
    private String email;

    /**
     * Giới tính của khách hàng.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    /**
     * Ngày sinh của khách hàng.
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * Số CMND/CCCD của khách hàng.
     */
    @Column(name = "identity_number", length = 50)
    private String identityNumber;

    /**
     * Địa chỉ của khách hàng.
     */
    @Column(length = 500)
    private String address;

    /**
     * Ghi chú thêm về khách hàng.
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
     * Mối quan hệ Một-Nhiều với thực thể SaleOrder.
     * Một khách hàng có thể có nhiều đơn hàng.
     */
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<SaleOrder> saleOrders = new ArrayList<>();

    /**
     * Mối quan hệ Một-Nhiều với thực thể TestDrive.
     * Một khách hàng có thể đăng ký nhiều lịch lái thử.
     */
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<TestDrive> testDrives = new ArrayList<>();

    // Constructors, Getters, and Setters...
    public Customer() {
    }

    public Customer(String customerCode, String fullName, String phone, String email, Gender gender,
                    LocalDate dateOfBirth, String identityNumber, String address, String note) {
        this.customerCode = customerCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.identityNumber = identityNumber;
        this.address = address;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public List<SaleOrder> getSaleOrders() {
        return saleOrders;
    }

    public void setSaleOrders(List<SaleOrder> saleOrders) {
        this.saleOrders = saleOrders;
    }

    public List<TestDrive> getTestDrives() {
        return testDrives;
    }

    public void setTestDrives(List<TestDrive> testDrives) {
        this.testDrives = testDrives;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", customerCode='" + customerCode + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
