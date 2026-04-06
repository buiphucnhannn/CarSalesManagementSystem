package vn.edu.ute.carsalesms.dao;

import java.util.List;
import java.util.Optional;
import vn.edu.ute.carsalesms.model.entity.Brand;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Car;
import vn.edu.ute.carsalesms.model.entity.CarCategory;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * Giao diện định nghĩa các phương thức truy cập dữ liệu cho các thực thể liên quan đến xe (Car, Brand, CarCategory).
 * DAO là viết tắt của Data Access Object, một mẫu thiết kế giúp tách biệt logic truy cập dữ liệu khỏi logic nghiệp vụ.
 */
public interface CarDao {

    /**
     * Tìm kiếm danh sách xe dựa trên từ khóa và bộ lọc trạng thái.
     * @param keyword Từ khóa tìm kiếm (có thể là tên xe, mã xe, v.v.).
     * @param statusFilter Lọc theo trạng thái (ví dụ: ACTIVE, INACTIVE).
     * @return Danh sách các đối tượng Car phù hợp.
     */
    List<Car> findCars(String keyword, Status statusFilter);

    /**
     * Tìm một xe dựa trên ID của nó.
     * @param id ID của xe cần tìm.
     * @return Một Optional chứa đối tượng Car nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Car> findById(Long id);

    /**
     * Tìm một xe dựa trên mã xe (carCode).
     * @param carCode Mã định danh duy nhất của xe.
     * @return Một Optional chứa đối tượng Car nếu tìm thấy.
     */
    Optional<Car> findByCode(String carCode);

    /**
     * Tìm kiếm danh sách các thương hiệu xe.
     * @param keyword Từ khóa tìm kiếm theo tên hoặc mã thương hiệu.
     * @param statusFilter Lọc theo trạng thái.
     * @return Danh sách các đối tượng Brand phù hợp.
     */
    List<Brand> findBrands(String keyword, Status statusFilter);

    /**
     * Tìm một thương hiệu dựa trên mã thương hiệu (brandCode).
     * @param brandCode Mã định danh của thương hiệu.
     * @return Một Optional chứa đối tượng Brand nếu tìm thấy.
     */
    Optional<Brand> findBrandByCode(String brandCode);

    /**
     * Tìm một thương hiệu dựa trên ID của nó.
     * @param id ID của thương hiệu.
     * @return Một Optional chứa đối tượng Brand nếu tìm thấy.
     */
    Optional<Brand> findBrandById(Long id);

    /**
     * Lưu (thêm mới hoặc cập nhật) một thương hiệu.
     * @param brand Đối tượng Brand cần lưu.
     * @return Đối tượng Brand đã được lưu.
     */
    Brand saveBrand(Brand brand);

    /**
     * Tìm kiếm danh sách các danh mục xe.
     * @param keyword Từ khóa tìm kiếm theo tên hoặc mã danh mục.
     * @param statusFilter Lọc theo trạng thái.
     * @return Danh sách các đối tượng CarCategory phù hợp.
     */
    List<CarCategory> findCategories(String keyword, Status statusFilter);

    /**
     * Tìm một danh mục xe dựa trên mã danh mục (categoryCode).
     * @param categoryCode Mã định danh của danh mục.
     * @return Một Optional chứa đối tượng CarCategory nếu tìm thấy.
     */
    Optional<CarCategory> findCategoryByCode(String categoryCode);

    /**
     * Tìm một danh mục xe dựa trên ID của nó.
     * @param id ID của danh mục.
     * @return Một Optional chứa đối tượng CarCategory nếu tìm thấy.
     */
    Optional<CarCategory> findCategoryById(Long id);

    /**
     * Lưu (thêm mới hoặc cập nhật) một danh mục xe.
     * @param category Đối tượng CarCategory cần lưu.
     * @return Đối tượng CarCategory đã được lưu.
     */
    CarCategory saveCategory(CarCategory category);

    /**
     * Tìm một chi nhánh dựa trên ID của nó.
     * @param id ID của chi nhánh.
     * @return Một Optional chứa đối tượng Branch nếu tìm thấy.
     */
    Optional<Branch> findBranchById(Long id);

    /**
     * Lấy danh sách tất cả các thương hiệu đang hoạt động (ACTIVE).
     * @return Danh sách các đối tượng Brand.
     */
    List<Brand> findActiveBrands();

    /**
     * Lấy danh sách tất cả các danh mục xe đang hoạt động (ACTIVE).
     * @return Danh sách các đối tượng CarCategory.
     */
    List<CarCategory> findActiveCategories();

    /**
     * Lấy danh sách tất cả các chi nhánh đang hoạt động (ACTIVE).
     * @return Danh sách các đối tượng Branch.
     */
    List<Branch> findActiveBranches();

    /**
     * Vô hiệu hóa (chuyển trạng thái sang INACTIVE) cho tất cả các xe thuộc một thương hiệu.
     * @param brandId ID của thương hiệu.
     * @return Số lượng xe đã bị vô hiệu hóa.
     */
    int deactivateCarsByBrandId(Long brandId);

    /**
     * Vô hiệu hóa (chuyển trạng thái sang INACTIVE) cho tất cả các xe thuộc một danh mục.
     * @param categoryId ID của danh mục.
     * @return Số lượng xe đã bị vô hiệu hóa.
     */
    int deactivateCarsByCategoryId(Long categoryId);

    /**
     * Lưu (thêm mới hoặc cập nhật) một xe.
     * @param car Đối tượng Car cần lưu.
     * @return Đối tượng Car đã được lưu.
     */
    Car save(Car car);
}
