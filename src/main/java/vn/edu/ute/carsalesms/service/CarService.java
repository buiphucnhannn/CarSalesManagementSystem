package vn.edu.ute.carsalesms.service;

import java.util.List;
import vn.edu.ute.carsalesms.model.dto.BrandCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BrandManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CategoryCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CategoryManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementMetadata;
import vn.edu.ute.carsalesms.model.enums.Status;

public interface CarService {

    List<CarManagementItem> getCars(String keyword, Status statusFilter);

    CarManagementMetadata getMetadata();

    CarManagementItem createCar(CarCommandRequest request);

    CarManagementItem updateCar(CarCommandRequest request);

    void deactivateCar(Long carId);

    List<BrandManagementItem> getBrands(String keyword, Status statusFilter);

    BrandManagementItem createBrand(BrandCommandRequest request);

    BrandManagementItem updateBrand(BrandCommandRequest request);

    void deactivateBrand(Long brandId);

    List<CategoryManagementItem> getCategories(String keyword, Status statusFilter);

    CategoryManagementItem createCategory(CategoryCommandRequest request);

    CategoryManagementItem updateCategory(CategoryCommandRequest request);

    void deactivateCategory(Long categoryId);
}

