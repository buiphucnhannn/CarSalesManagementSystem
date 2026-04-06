package vn.edu.ute.carsalesms.controller;

import java.util.List;
import java.util.Objects;
import vn.edu.ute.carsalesms.model.dto.BrandCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BrandManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CarManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.CategoryCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CategoryManagementItem;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.CarService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;

/**
 * CarManagementController xử lý các yêu cầu liên quan đến quản lý ô tô, thương hiệu và danh mục.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic quản lý ô tô.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào các giao diện
 * (CarService, AuditLogService) thay vì các triển khai cụ thể.
 */
public class CarManagementController {

    private final CarService carService;
    private final AuditLogService auditLogService;

    /**
     * Xây dựng một CarManagementController mới với CarService đã cho.
     * @param carService dịch vụ sẽ được sử dụng để quản lý ô tô.
     */
    public CarManagementController(CarService carService) {
        this(carService, new NoOpAuditLogService());
    }

    /**
     * Xây dựng một CarManagementController mới với CarService và AuditLogService đã cho.
     * @param carService dịch vụ sẽ được sử dụng để quản lý ô tô.
     * @param auditLogService dịch vụ sẽ được sử dụng để ghi lại các hành động.
     */
    public CarManagementController(CarService carService, AuditLogService auditLogService) {
        this.carService = Objects.requireNonNull(carService, "carService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    /**
     * Tải tất cả các ô tô khớp với từ khóa và bộ lọc trạng thái đã cho.
     * @param keyword từ khóa để tìm kiếm.
     * @param statusFilter bộ lọc trạng thái.
     * @return danh sách các mục quản lý ô tô.
     */
    public List<CarManagementItem> loadCars(String keyword, Status statusFilter) {
        return carService.getCars(keyword, statusFilter);
    }

    /**
     * Tải siêu dữ liệu cho quản lý ô tô.
     * @return siêu dữ liệu quản lý ô tô.
     */
    public CarManagementMetadata loadMetadata() {
        return carService.getMetadata();
    }

    /**
     * Tạo một ô tô mới.
     * @param request yêu cầu tạo ô tô.
     * @return mục quản lý ô tô đã tạo.
     */
    public CarManagementItem createCar(CarCommandRequest request) {
        CarManagementItem created = carService.createCar(request);
        auditLogService.log("CREATE", "CAR", created.id(), null, request.toString());
        return created;
    }

    /**
     * Cập nhật một ô tô hiện có.
     * @param request yêu cầu cập nhật ô tô.
     * @return mục quản lý ô tô đã cập nhật.
     */
    public CarManagementItem updateCar(CarCommandRequest request) {
        CarManagementItem updated = carService.updateCar(request);
        auditLogService.log("UPDATE", "CAR", updated.id(), null, request.toString());
        return updated;
    }

    /**
     * Hủy kích hoạt một ô tô.
     * @param carId ID của ô tô cần hủy kích hoạt.
     */
    public void deactivateCar(Long carId) {
        carService.deactivateCar(carId);
        auditLogService.log("DEACTIVATE", "CAR", carId, null, "status=INACTIVE");
    }

    /**
     * Tải tất cả các thương hiệu khớp với từ khóa và bộ lọc trạng thái đã cho.
     * @param keyword từ khóa để tìm kiếm.
     * @param statusFilter bộ lọc trạng thái.
     * @return danh sách các mục quản lý thương hiệu.
     */
    public List<BrandManagementItem> loadBrands(String keyword, Status statusFilter) {
        return carService.getBrands(keyword, statusFilter);
    }

    /**
     * Tạo một thương hiệu mới.
     * @param request yêu cầu tạo thương hiệu.
     * @return mục quản lý thương hiệu đã tạo.
     */
    public BrandManagementItem createBrand(BrandCommandRequest request) {
        BrandManagementItem created = carService.createBrand(request);
        auditLogService.log("CREATE", "BRAND", created.id(), null, request.toString());
        return created;
    }

    /**
     * Cập nhật một thương hiệu hiện có.
     * @param request yêu cầu cập nhật thương hiệu.
     * @return mục quản lý thương hiệu đã cập nhật.
     */
    public BrandManagementItem updateBrand(BrandCommandRequest request) {
        BrandManagementItem updated = carService.updateBrand(request);
        auditLogService.log("UPDATE", "BRAND", updated.id(), null, request.toString());
        return updated;
    }

    /**
     * Hủy kích hoạt một thương hiệu.
     * @param brandId ID của thương hiệu cần hủy kích hoạt.
     */
    public void deactivateBrand(Long brandId) {
        carService.deactivateBrand(brandId);
        auditLogService.log("DEACTIVATE", "BRAND", brandId, null, "status=INACTIVE");
    }

    /**
     * Tải tất cả các danh mục khớp với từ khóa và bộ lọc trạng thái đã cho.
     * @param keyword từ khóa để tìm kiếm.
     * @param statusFilter bộ lọc trạng thái.
     * @return danh sách các mục quản lý danh mục.
     */
    public List<CategoryManagementItem> loadCategories(String keyword, Status statusFilter) {
        return carService.getCategories(keyword, statusFilter);
    }

    /**
     * Tạo một danh mục mới.
     * @param request yêu cầu tạo danh mục.
     * @return mục quản lý danh mục đã tạo.
     */
    public CategoryManagementItem createCategory(CategoryCommandRequest request) {
        CategoryManagementItem created = carService.createCategory(request);
        auditLogService.log("CREATE", "CAR_CATEGORY", created.id(), null, request.toString());
        return created;
    }

    /**
     * Cập nhật một danh mục hiện có.
     * @param request yêu cầu cập nhật danh mục.
     * @return mục quản lý danh mục đã cập nhật.
     */
    public CategoryManagementItem updateCategory(CategoryCommandRequest request) {
        CategoryManagementItem updated = carService.updateCategory(request);
        auditLogService.log("UPDATE", "CAR_CATEGORY", updated.id(), null, request.toString());
        return updated;
    }

    /**
     * Hủy kích hoạt một danh mục.
     * @param categoryId ID của danh mục cần hủy kích hoạt.
     */
    public void deactivateCategory(Long categoryId) {
        carService.deactivateCategory(categoryId);
        auditLogService.log("DEACTIVATE", "CAR_CATEGORY", categoryId, null, "status=INACTIVE");
    }
}
