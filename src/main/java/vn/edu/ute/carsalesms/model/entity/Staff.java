package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng staffs.
 * Lưu thông tin nhân viên và tài khoản đăng nhập nội bộ hệ thống.
 */
@Entity
@Table(name = "staffs")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_code", nullable = false, unique = true, length = 50)
    private String staffCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StaffRole role;

    /**
     * Quan hệ nhiều - một:
     * Nhiều nhân viên có thể thuộc cùng một chi nhánh.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Quan hệ một - nhiều:
     * Một nhân viên có thể tạo nhiều đơn bán.
     */
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    private List<SaleOrder> saleOrders = new ArrayList<>();

    /**
     * Quan hệ một - nhiều:
     * Một nhân viên có thể phụ trách nhiều lịch lái thử.
     */
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    private List<TestDrive> testDrives = new ArrayList<>();

    /**
     * Quan hệ một - nhiều:
     * Một nhân viên có thể phát sinh nhiều log thao tác.
     */
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs = new ArrayList<>();

    public Staff() {
    }

    public Staff(String staffCode, String fullName, String username, String password, String email,
                 String phone, StaffRole role, Branch branch, Status status) {
        this.staffCode = staffCode;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.branch = branch;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public StaffRole getRole() {
        return role;
    }

    public void setRole(StaffRole role) {
        this.role = role;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
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

    public List<TestDrive> getTestDrives() {
        return testDrives;
    }

    public void setTestDrives(List<TestDrive> testDrives) {
        this.testDrives = testDrives;
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "id=" + id +
                ", staffCode='" + staffCode + '\'' +
                ", fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}