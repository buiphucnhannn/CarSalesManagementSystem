package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Entity, đại diện cho bảng `staffs` trong cơ sở dữ liệu.
 * Lưu trữ các thông tin nghiệp vụ của một nhân viên, không bao gồm thông tin đăng nhập.
 */
@Entity
@Table(name = "staffs")
public class Staff {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã nhân viên, là một định danh duy nhất, không được null.
     */
    @Column(name = "staff_code", nullable = false, unique = true, length = 50)
    private String staffCode;

    /**
     * Tên đầy đủ của nhân viên.
     */
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    /**
     * Email của nhân viên, là duy nhất.
     */
    @Column(unique = true, length = 100)
    private String email;

    /**
     * Số điện thoại của nhân viên.
     */
    @Column(length = 20)
    private String phone;

    /**
     * Vai trò của nhân viên trong hệ thống (ví dụ: ADMIN, STAFF).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StaffRole role;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Branch.
     * Nhiều nhân viên có thể làm việc tại cùng một chi nhánh.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    /**
     * Trạng thái của nhân viên (ví dụ: ACTIVE - đang làm việc, INACTIVE - đã nghỉ việc).
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
     * Mối quan hệ Một-Một (One-to-One) với thực thể Account.
     * Mỗi nhân viên có thể có một và chỉ một tài khoản đăng nhập.
     * `mappedBy = "staff"`: Mối quan hệ này được quản lý bởi thuộc tính `staff` trong lớp `Account`.
     */
    @OneToOne(mappedBy = "staff", fetch = FetchType.LAZY)
    private vn.edu.ute.carsalesms.model.entity.Account account;

    /**
     * Mối quan hệ Một-Nhiều với thực thể SaleOrder.
     * Một nhân viên có thể tạo và quản lý nhiều đơn hàng.
     */
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    private List<SaleOrder> saleOrders = new ArrayList<>();

    /**
     * Mối quan hệ Một-Nhiều với thực thể TestDrive.
     * Một nhân viên có thể phụ trách nhiều lịch hẹn lái thử.
     */
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    private List<TestDrive> testDrives = new ArrayList<>();

    /**
     * Mối quan hệ Một-Nhiều với thực thể AuditLog.
     * Một nhân viên có thể thực hiện nhiều hành động được ghi lại trong nhật ký hệ thống.
     */
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs = new ArrayList<>();

    // Constructors, Getters, and Setters...
    public Staff() {
    }

    public Staff(String staffCode, String fullName, String email,
                 String phone, StaffRole role, Branch branch, Status status) {
        this.staffCode = staffCode;
        this.fullName = fullName;
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

    public vn.edu.ute.carsalesms.model.entity.Account getAccount() {
        return account;
    }

    public void setAccount(vn.edu.ute.carsalesms.model.entity.Account account) {
        this.account = account;
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
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
