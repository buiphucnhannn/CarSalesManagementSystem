package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Entity, đại diện cho bảng `brands` trong cơ sở dữ liệu.
 * Mỗi đối tượng của lớp này tương ứng với một dòng trong bảng, lưu trữ thông tin về một thương hiệu xe.
 */
@Entity
@Table(name = "brands")
public class Brand {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã thương hiệu, là một định danh duy nhất, không được null.
     */
    @Column(name = "brand_code", nullable = false, unique = true, length = 50)
    private String brandCode;

    /**
     * Tên thương hiệu, cũng là duy nhất và không được null.
     */
    @Column(name = "brand_name", nullable = false, unique = true, length = 255)
    private String brandName;

    /**
     * Quốc gia xuất xứ của thương hiệu.
     */
    @Column(length = 100)
    private String country;

    /**
     * Mô tả chi tiết về thương hiệu.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Trạng thái của thương hiệu (ví dụ: ACTIVE - đang hợp tác, INACTIVE - đã ngừng).
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
     * Mối quan hệ Một-Nhiều (One-to-Many) với thực thể Car.
     * Một thương hiệu có thể có nhiều mẫu xe.
     * `mappedBy = "brand"`: Mối quan hệ này được quản lý bởi thuộc tính `brand` trong lớp `Car`.
     * `fetch = FetchType.LAZY`: Danh sách các xe sẽ chỉ được tải khi có yêu cầu truy cập đến nó.
     */
    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<>();

    // Constructors, Getters, and Setters...
    public Brand() {
    }

    public Brand(String brandCode, String brandName, String country, String description, Status status) {
        this.brandCode = brandCode;
        this.brandName = brandName;
        this.country = country;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getBrandCode() {
        return brandCode;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    @Override
    public String toString() {
        return "Brand{" +
                "id=" + id +
                ", brandCode='" + brandCode + '\'' +
                ", brandName='" + brandName + '\'' +
                ", country='" + country + '\'' +
                ", status=" + status +
                '}';
    }
}
