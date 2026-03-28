package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng brands.
 * Lưu thông tin thương hiệu xe trong hệ thống.
 */
@Entity
@Table(name = "brands")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_code", nullable = false, unique = true, length = 50)
    private String brandCode;

    @Column(name = "brand_name", nullable = false, unique = true, length = 255)
    private String brandName;

    @Column(length = 100)
    private String country;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Quan hệ một - nhiều:
     * Một thương hiệu có thể có nhiều xe.
     */
    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<>();

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