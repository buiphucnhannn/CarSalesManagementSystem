package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;

import java.time.LocalDateTime;

/**
 * Lớp Entity, đại diện cho bảng `test_drives` trong cơ sở dữ liệu.
 * Lưu trữ thông tin về một lịch hẹn lái thử xe giữa khách hàng và showroom.
 */
@Entity
@Table(name = "test_drives")
public class TestDrive {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã lịch hẹn lái thử, là một định danh duy nhất, không được null.
     */
    @Column(name = "test_drive_code", nullable = false, unique = true, length = 50)
    private String testDriveCode;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Customer.
     * Nhiều lịch hẹn có thể được đăng ký bởi cùng một khách hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Car.
     * Nhiều lịch hẹn có thể đăng ký lái thử cùng một mẫu xe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Staff.
     * Nhiều lịch hẹn có thể được phụ trách bởi cùng một nhân viên.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    /**
     * Thời gian dự kiến diễn ra buổi lái thử.
     */
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    /**
     * Kết quả hoặc phản hồi của khách hàng sau buổi lái thử.
     */
    @Column(length = 255)
    private String result;

    /**
     * Trạng thái của lịch hẹn (ví dụ: SCHEDULED - đã lên lịch, COMPLETED - đã hoàn thành, CANCELLED - đã hủy).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TestDriveStatus status = TestDriveStatus.SCHEDULED;

    /**
     * Ghi chú cho lịch hẹn.
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

    // Constructors, Getters, and Setters...
    public TestDrive() {
    }

    public TestDrive(String testDriveCode, Customer customer, Car car, Staff staff,
                     LocalDateTime scheduledTime, String result, TestDriveStatus status, String note) {
        this.testDriveCode = testDriveCode;
        this.customer = customer;
        this.car = car;
        this.staff = staff;
        this.scheduledTime = scheduledTime;
        this.result = result;
        this.status = status;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public String getTestDriveCode() {
        return testDriveCode;
    }

    public void setTestDriveCode(String testDriveCode) {
        this.testDriveCode = testDriveCode;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public TestDriveStatus getStatus() {
        return status;
    }

    public void setStatus(TestDriveStatus status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "TestDrive{" +
                "id=" + id +
                ", testDriveCode='" + testDriveCode + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", result='" + result + '\'' +
                ", status=" + status +
                '}';
    }
}
