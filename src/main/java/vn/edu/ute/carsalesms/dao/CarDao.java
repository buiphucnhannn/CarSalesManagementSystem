package vn.edu.ute.carsalesms.dao;

import java.util.List;
import java.util.Optional;
import vn.edu.ute.carsalesms.model.entity.Brand;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Car;
import vn.edu.ute.carsalesms.model.entity.CarCategory;
import vn.edu.ute.carsalesms.model.enums.Status;

public interface CarDao {

    List<Car> findCars(String keyword, Status statusFilter);

    Optional<Car> findById(Long id);

    Optional<Car> findByCode(String carCode);

    List<Brand> findBrands(String keyword, Status statusFilter);

    Optional<Brand> findBrandByCode(String brandCode);

    Optional<Brand> findBrandById(Long id);

    Brand saveBrand(Brand brand);

    List<CarCategory> findCategories(String keyword, Status statusFilter);

    Optional<CarCategory> findCategoryByCode(String categoryCode);

    Optional<CarCategory> findCategoryById(Long id);

    CarCategory saveCategory(CarCategory category);

    Optional<Branch> findBranchById(Long id);

    List<Brand> findActiveBrands();

    List<CarCategory> findActiveCategories();

    List<Branch> findActiveBranches();

    int deactivateCarsByBrandId(Long brandId);

    int deactivateCarsByCategoryId(Long categoryId);

    Car save(Car car);
}

