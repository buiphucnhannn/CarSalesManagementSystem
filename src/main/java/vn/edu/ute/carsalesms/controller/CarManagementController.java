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

public class CarManagementController {

    private final CarService carService;
    private final AuditLogService auditLogService;

    public CarManagementController(CarService carService) {
        this(carService, new NoOpAuditLogService());
    }

    public CarManagementController(CarService carService, AuditLogService auditLogService) {
        this.carService = Objects.requireNonNull(carService, "carService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    public List<CarManagementItem> loadCars(String keyword, Status statusFilter) {
        return carService.getCars(keyword, statusFilter);
    }

    public CarManagementMetadata loadMetadata() {
        return carService.getMetadata();
    }

    public CarManagementItem createCar(CarCommandRequest request) {
        CarManagementItem created = carService.createCar(request);
        auditLogService.log("CREATE", "CAR", created.id(), null, request.toString());
        return created;
    }

    public CarManagementItem updateCar(CarCommandRequest request) {
        CarManagementItem updated = carService.updateCar(request);
        auditLogService.log("UPDATE", "CAR", updated.id(), null, request.toString());
        return updated;
    }

    public void deactivateCar(Long carId) {
        carService.deactivateCar(carId);
        auditLogService.log("DEACTIVATE", "CAR", carId, null, "status=INACTIVE");
    }

    public List<BrandManagementItem> loadBrands(String keyword, Status statusFilter) {
        return carService.getBrands(keyword, statusFilter);
    }

    public BrandManagementItem createBrand(BrandCommandRequest request) {
        BrandManagementItem created = carService.createBrand(request);
        auditLogService.log("CREATE", "BRAND", created.id(), null, request.toString());
        return created;
    }

    public BrandManagementItem updateBrand(BrandCommandRequest request) {
        BrandManagementItem updated = carService.updateBrand(request);
        auditLogService.log("UPDATE", "BRAND", updated.id(), null, request.toString());
        return updated;
    }

    public void deactivateBrand(Long brandId) {
        carService.deactivateBrand(brandId);
        auditLogService.log("DEACTIVATE", "BRAND", brandId, null, "status=INACTIVE");
    }

    public List<CategoryManagementItem> loadCategories(String keyword, Status statusFilter) {
        return carService.getCategories(keyword, statusFilter);
    }

    public CategoryManagementItem createCategory(CategoryCommandRequest request) {
        CategoryManagementItem created = carService.createCategory(request);
        auditLogService.log("CREATE", "CAR_CATEGORY", created.id(), null, request.toString());
        return created;
    }

    public CategoryManagementItem updateCategory(CategoryCommandRequest request) {
        CategoryManagementItem updated = carService.updateCategory(request);
        auditLogService.log("UPDATE", "CAR_CATEGORY", updated.id(), null, request.toString());
        return updated;
    }

    public void deactivateCategory(Long categoryId) {
        carService.deactivateCategory(categoryId);
        auditLogService.log("DEACTIVATE", "CAR_CATEGORY", categoryId, null, "status=INACTIVE");
    }
}

