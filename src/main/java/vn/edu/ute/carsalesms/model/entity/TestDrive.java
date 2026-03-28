package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng test_drives.
 * Lưu thông tin lịch hẹn lái thử giữa khách hàng và showroom.
 */
@Entity
@Table(name = "test_drives")
public class TestDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_drive_code", nullable = false, unique = true, length = 50)
    private String testDriveCode;

    /**
     * Quan hệ nhiều - một:
     * Nhiều lịch lái thử có thể thuộc cùng một khách hàng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Quan hệ nhiều - một:
     * Nhiều lịch lái thử có thể tham chiếu cùng một xe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    /**
     * Quan hệ nhiều - một:
     * Nhiều lịch lái thử có thể do cùng một nhân viên phụ trách.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(length = 255)
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TestDriveStatus status = TestDriveStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

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