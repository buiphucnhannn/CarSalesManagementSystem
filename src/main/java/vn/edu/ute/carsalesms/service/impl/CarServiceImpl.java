package vn.edu.ute.carsalesms.service.impl;

import java.util.List;
import java.util.Objects;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.model.dto.BrandCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BrandManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CarManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.CategoryCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CategoryManagementItem;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.CarBrandService;
import vn.edu.ute.carsalesms.service.CarCategoryService;
import vn.edu.ute.carsalesms.service.CarInventoryService;
import vn.edu.ute.carsalesms.service.CarService;
import vn.edu.ute.carsalesms.session.CurrentSessionContextAdapter;
import vn.edu.ute.carsalesms.session.UserSessionContext;

public class CarServiceImpl implements CarService {

    private final CarInventoryService inventoryService;
    private final CarBrandService brandService;
    private final CarCategoryService categoryService;

    public CarServiceImpl(CarDao carDao) {
        this(carDao, new CurrentSessionContextAdapter());
    }

    public CarServiceImpl(CarDao carDao, UserSessionContext sessionContext) {
        this(
                new CarInventoryServiceImpl(carDao, sessionContext),
                new CarBrandServiceImpl(carDao, sessionContext),
                new CarCategoryServiceImpl(carDao, sessionContext)
        );
    }

    public CarServiceImpl(CarInventoryService inventoryService,
                          CarBrandService brandService,
                          CarCategoryService categoryService) {
        this.inventoryService = Objects.requireNonNull(inventoryService, "inventoryService is required");
        this.brandService = Objects.requireNonNull(brandService, "brandService is required");
        this.categoryService = Objects.requireNonNull(categoryService, "categoryService is required");
    }

    @Override
    public List<CarManagementItem> getCars(String keyword, Status statusFilter) {
        return inventoryService.getCars(keyword, statusFilter);
    }

    @Override
    public CarManagementMetadata getMetadata() {
        return inventoryService.getMetadata();
    }

    @Override
    public CarManagementItem createCar(CarCommandRequest request) {
        return inventoryService.createCar(request);
    }

    @Override
    public CarManagementItem updateCar(CarCommandRequest request) {
        return inventoryService.updateCar(request);
    }

    @Override
    public void deactivateCar(Long carId) {
        inventoryService.deactivateCar(carId);
    }

    @Override
    public List<BrandManagementItem> getBrands(String keyword, Status statusFilter) {
        return brandService.getBrands(keyword, statusFilter);
    }

    @Override
    public BrandManagementItem createBrand(BrandCommandRequest request) {
        return brandService.createBrand(request);
    }

    @Override
    public BrandManagementItem updateBrand(BrandCommandRequest request) {
        return brandService.updateBrand(request);
    }

    @Override
    public void deactivateBrand(Long brandId) {
        brandService.deactivateBrand(brandId);
    }

    @Override
    public List<CategoryManagementItem> getCategories(String keyword, Status statusFilter) {
        return categoryService.getCategories(keyword, statusFilter);
    }

    @Override
    public CategoryManagementItem createCategory(CategoryCommandRequest request) {
        return categoryService.createCategory(request);
    }

    @Override
    public CategoryManagementItem updateCategory(CategoryCommandRequest request) {
        return categoryService.updateCategory(request);
    }

    @Override
    public void deactivateCategory(Long categoryId) {
        categoryService.deactivateCategory(categoryId);
    }
}

