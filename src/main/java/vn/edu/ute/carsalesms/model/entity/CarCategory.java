package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng car_categories.
 * Lưu thông tin phân loại xe như Sedan, SUV, Hatchback...
 */
@Entity
@Table(name = "car_categories")
public class CarCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, unique = true, length = 255)
    private String categoryName;

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
     * Một danh mục xe có thể chứa nhiều xe.
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<>();

    public CarCategory() {
    }

    public CarCategory(String categoryCode, String categoryName, String description, Status status) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
        return "CarCategory{" +
                "id=" + id +
                ", categoryCode='" + categoryCode + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", status=" + status +
                '}';
    }
}