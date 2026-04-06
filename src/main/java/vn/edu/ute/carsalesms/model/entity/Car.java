package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Entity, đại diện cho bảng `cars` trong cơ sở dữ liệu.
 * Mỗi đối tượng của lớp này tương ứng với một dòng trong bảng, lưu trữ thông tin chi tiết về một loại xe đang được kinh doanh tại showroom.
 * Hệ thống hiện tại quản lý xe theo mô hình sản phẩm (product model), tức là mỗi bản ghi đại diện cho một mẫu xe với số lượng tồn kho, chứ không phải một chiếc xe cụ thể.
 */
@Entity
@Table(name = "cars")
public class Car {

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã xe, là một định danh duy nhất, không được null.
     */
    @Column(name = "car_code", nullable = false, unique = true, length = 50)
    private String carCode;

    /**
     * Tên đầy đủ của xe.
     */
    @Column(name = "car_name", nullable = false, length = 255)
    private String carName;

    /**
     * Mối quan hệ Nhiều-Một (Many-to-One) với thực thể Brand.
     * Nhiều xe có thể thuộc về cùng một thương hiệu.
     * `fetch = FetchType.LAZY`: Dữ liệu của Brand sẽ chỉ được tải từ DB khi thực sự cần thiết.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    /**
     * Mối quan hệ Nhiều-Một với thực thể CarCategory.
     * Nhiều xe có thể thuộc cùng một danh mục (ví dụ: SUV, Sedan).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CarCategory category;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Branch.
     * Nhiều xe có thể được quản lý và tồn kho tại cùng một chi nhánh.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(length = 50)
    private String color;

    @Column(name = "seat_count")
    private Integer seatCount;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    @Column(length = 50)
    private String transmission;

    @Column(length = 100)
    private String origin;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    /**
     * Giá nhập kho của xe.
     * `precision` và `scale` dùng để định nghĩa độ chính xác cho kiểu dữ liệu số thập phân trong DB.
     */
    @Column(name = "import_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal importPrice = BigDecimal.ZERO;

    /**
     * Giá bán niêm yết của xe.
     */
    @Column(name = "sale_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal salePrice = BigDecimal.ZERO;

    /**
     * Tổng số lượng xe đã nhập.
     */
    @Column(nullable = false)
    private Integer quantity = 0;

    /**
     * Số lượng xe còn lại có sẵn để bán.
     */
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    /**
     * Mô tả chi tiết về xe, sử dụng kiểu TEXT trong DB để lưu trữ chuỗi dài.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Trạng thái của xe (ví dụ: ACTIVE - đang kinh doanh, INACTIVE - ngừng kinh doanh).
     * `EnumType.STRING` lưu tên của enum (ví dụ: "ACTIVE") vào DB thay vì vị trí số (0, 1).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    /**
     * Thời điểm bản ghi được tạo, được quản lý tự động bởi cơ sở dữ liệu.
     * `insertable = false, updatable = false`: JPA sẽ không chèn hoặc cập nhật giá trị cho cột này.
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời điểm bản ghi được cập nhật lần cuối, được quản lý tự động bởi cơ sở dữ liệu.
     */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Mối quan hệ Một-Nhiều (One-to-Many) với SaleOrderDetail.
     * Một mẫu xe có thể xuất hiện trong nhiều dòng chi tiết của các đơn hàng khác nhau.
     * `mappedBy = "car"`: Chỉ ra rằng mối quan hệ này được quản lý bởi thuộc tính `car` trong lớp `SaleOrderDetail`.
     */
    @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
    private List<SaleOrderDetail> saleOrderDetails = new ArrayList<>();

    /**
     * Mối quan hệ Một-Nhiều với TestDrive.
     * Một mẫu xe có thể được đặt cho nhiều lịch lái thử.
     */
    @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
    private List<TestDrive> testDrives = new ArrayList<>();

    // Constructors, Getters, and Setters...
    public Car() {
    }

    public Car(String carCode, String carName, Brand brand, CarCategory category, Branch branch, String color,
               Integer seatCount, String fuelType, String transmission, String origin, Integer manufactureYear,
               BigDecimal importPrice, BigDecimal salePrice, Integer quantity, Integer availableQuantity,
               String description, Status status) {
        this.carCode = carCode;
        this.carName = carName;
        this.brand = brand;
        this.category = category;
        this.branch = branch;
        this.color = color;
        this.seatCount = seatCount;
        this.fuelType = fuelType;
        this.transmission = transmission;
        this.origin = origin;
        this.manufactureYear = manufactureYear;
        this.importPrice = importPrice;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCarCode() {
        return carCode;
    }

    public void setCarCode(String carCode) {
        this.carCode = carCode;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public CarCategory getCategory() {
        return category;
    }

    public void setCategory(CarCategory category) {
        this.category = category;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Integer seatCount) {
        this.seatCount = seatCount;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Integer getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(Integer manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
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

    public List<SaleOrderDetail> getSaleOrderDetails() {
        return saleOrderDetails;
    }

    public void setSaleOrderDetails(List<SaleOrderDetail> saleOrderDetails) {
        this.saleOrderDetails = saleOrderDetails;
    }

    public List<TestDrive> getTestDrives() {
        return testDrives;
    }

    public void setTestDrives(List<TestDrive> testDrives) {
        this.testDrives = testDrives;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", carCode='" + carCode + '\'' +
                ", carName='" + carName + '\'' +
                ", color='" + color + '\'' +
                ", seatCount=" + seatCount +
                ", salePrice=" + salePrice +
                ", quantity=" + quantity +
                ", availableQuantity=" + availableQuantity +
                ", status=" + status +
                '}';
    }
}
