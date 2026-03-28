package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng customers.
 * Lưu thông tin khách hàng của showroom.
 * Khách hàng trong hệ thống này không có chức năng đăng nhập.
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", nullable = false, unique = true, length = 50)
    private String customerCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "identity_number", length = 50)
    private String identityNumber;

    @Column(length = 500)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Quan hệ một - nhiều:
     * Một khách hàng có thể có nhiều đơn bán.
     */
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<SaleOrder> saleOrders = new ArrayList<>();

    /**
     * Quan hệ một - nhiều:
     * Một khách hàng có thể có nhiều lịch lái thử.
     */
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<TestDrive> testDrives = new ArrayList<>();

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