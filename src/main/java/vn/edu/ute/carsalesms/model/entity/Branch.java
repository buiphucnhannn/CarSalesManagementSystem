package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Entity, đại diện cho bảng `branches` trong cơ sở dữ liệu.
 * Lưu trữ thông tin chi tiết về các chi nhánh hoặc showroom của hệ thống.
 */
@Entity
@Table(name = "branches")
public class Branch {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã chi nhánh, là một định danh duy nhất, không được null.
     */
    @Column(name = "branch_code", nullable = false, unique = true, length = 50)
    private String branchCode;

    /**
     * Tên chi nhánh, không được null.
     */
    @Column(name = "branch_name", nullable = false, length = 255)
    private String branchName;

    /**
     * Địa chỉ của chi nhánh.
     */
    @Column(length = 500)
    private String address;

    /**
     * Số điện thoại liên hệ của chi nhánh.
     */
    @Column(length = 20)
    private String phone;

    /**
     * Địa chỉ email của chi nhánh.
     */
    @Column(length = 100)
    private String email;

    /**
     * Trạng thái hoạt động của chi nhánh (ví dụ: ACTIVE - đang hoạt động, INACTIVE - tạm ngừng).
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
     * Mối quan hệ Một-Nhiều (One-to-Many) với thực thể Staff.
     * Một chi nhánh có thể có nhiều nhân viên làm việc.
     * `mappedBy = "branch"`: Mối quan hệ này được quản lý bởi thuộc tính `branch` trong lớp `Staff`.
     */
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private List<Staff> staffs = new ArrayList<>();

    /**
     * Mối quan hệ Một-Nhiều với thực thể Car.
     * Một chi nhánh có thể quản lý tồn kho của nhiều mẫu xe.
     */
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<>();

    // Constructors, Getters, and Setters...
    public Branch() {
    }

    public Branch(String branchCode, String branchName, String address, String phone, String email, Status status) {
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public List<Staff> getStaffs() {
        return staffs;
    }

    public void setStaffs(List<Staff> staffs) {
        this.staffs = staffs;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    @Override
    public String toString() {
        return "Branch{" +
                "id=" + id +
                ", branchCode='" + branchCode + '\'' +
                ", branchName='" + branchName + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }
}
