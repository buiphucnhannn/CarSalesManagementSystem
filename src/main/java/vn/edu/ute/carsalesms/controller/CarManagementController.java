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
import vn.edu.ute.carsalesms.service.CarService;

public class CarManagementController {

    private final CarService carService;

    public CarManagementController(CarService carService) {
        this.carService = Objects.requireNonNull(carService, "carService is required");
    }

    public List<CarManagementItem> loadCars(String keyword, Status statusFilter) {
        return carService.getCars(keyword, statusFilter);
    }

    public CarManagementMetadata loadMetadata() {
        return carService.getMetadata();
    }

    public CarManagementItem createCar(CarCommandRequest request) {
        return carService.createCar(request);
    }

    public CarManagementItem updateCar(CarCommandRequest request) {
        return carService.updateCar(request);
    }

    public void deactivateCar(Long carId) {
        carService.deactivateCar(carId);
    }

    public List<BrandManagementItem> loadBrands(String keyword, Status statusFilter) {
        return carService.getBrands(keyword, statusFilter);
    }

    public BrandManagementItem createBrand(BrandCommandRequest request) {
        return carService.createBrand(request);
    }

    public BrandManagementItem updateBrand(BrandCommandRequest request) {
        return carService.updateBrand(request);
    }

    public void deactivateBrand(Long brandId) {
        carService.deactivateBrand(brandId);
    }

    public List<CategoryManagementItem> loadCategories(String keyword, Status statusFilter) {
        return carService.getCategories(keyword, statusFilter);
    }

    public CategoryManagementItem createCategory(CategoryCommandRequest request) {
        return carService.createCategory(request);
    }

    public CategoryManagementItem updateCategory(CategoryCommandRequest request) {
        return carService.updateCategory(request);
    }

    public void deactivateCategory(Long categoryId) {
        carService.deactivateCategory(categoryId);
    }
}

