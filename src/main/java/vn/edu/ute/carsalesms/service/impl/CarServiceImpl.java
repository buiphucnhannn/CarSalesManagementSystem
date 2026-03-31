package vn.edu.ute.carsalesms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.model.dto.BrandCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BrandManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CarLookupItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.CategoryCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CategoryManagementItem;
import vn.edu.ute.carsalesms.model.entity.Brand;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Car;
import vn.edu.ute.carsalesms.model.entity.CarCategory;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.CarService;
import vn.edu.ute.carsalesms.util.CodeGeneratorUtil;

public class CarServiceImpl implements CarService {

    private final CarDao carDao;

    public CarServiceImpl(CarDao carDao) {
        this.carDao = Objects.requireNonNull(carDao, "carDao is required");
    }

    @Override
    public List<CarManagementItem> getCars(String keyword, Status statusFilter) {
        return carDao.findCars(keyword, statusFilter).stream()
                .map(this::toCarItem)
                .toList();
    }

    @Override
    public CarManagementMetadata getMetadata() {
        return new CarManagementMetadata(
                carDao.findActiveBrands().stream()
                        .map(brand -> new CarLookupItem(brand.getId(), brand.getBrandCode(), brand.getBrandName()))
                        .toList(),
                carDao.findActiveCategories().stream()
                        .map(category -> new CarLookupItem(category.getId(), category.getCategoryCode(), category.getCategoryName()))
                        .toList(),
                carDao.findActiveBranches().stream()
                        .map(branch -> new CarLookupItem(branch.getId(), branch.getBranchCode(), branch.getBranchName()))
                        .toList(),
                nextCarCode(),
                nextBrandCode(),
                nextCategoryCode()
        );
    }

    @Override
    public CarManagementItem createCar(CarCommandRequest request) {
        CarCommandRequest validated = validateRequest(request, false);
        carDao.findByCode(validated.carCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã xe đã tồn tại: " + validated.carCode());
        });

        Car car = new Car();
        applyData(car, validated);
        return toCarItem(carDao.save(car));
    }

    @Override
    public CarManagementItem updateCar(CarCommandRequest request) {
        CarCommandRequest validated = validateRequest(request, true);
        Car car = carDao.findById(validated.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe cần cập nhật."));

        carDao.findByCode(validated.carCode())
                .filter(existing -> !existing.getId().equals(validated.id()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã xe đã tồn tại: " + validated.carCode());
                });

        applyData(car, validated);
        return toCarItem(carDao.save(car));
    }

    @Override
    public void deactivateCar(Long carId) {
        if (carId == null) {
            throw new IllegalArgumentException("Xe không hợp lệ.");
        }
        Car car = carDao.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe cần ngừng kinh doanh."));
        car.setStatus(Status.INACTIVE);
        carDao.save(car);
    }

    @Override
    public List<BrandManagementItem> getBrands(String keyword, Status statusFilter) {
        return carDao.findBrands(keyword, statusFilter).stream()
                .map(this::toBrandItem)
                .toList();
    }

    @Override
    public BrandManagementItem createBrand(BrandCommandRequest request) {
        BrandCommandRequest validated = validateBrandRequest(request, false);
        carDao.findBrandByCode(validated.brandCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã hãng đã tồn tại: " + validated.brandCode());
        });

        Brand brand = new Brand();
        applyBrandData(brand, validated);
        return toBrandItem(carDao.saveBrand(brand));
    }

    @Override
    public BrandManagementItem updateBrand(BrandCommandRequest request) {
        BrandCommandRequest validated = validateBrandRequest(request, true);
        Brand brand = carDao.findBrandById(validated.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hãng xe cần cập nhật."));
        Status previousStatus = brand.getStatus();

        carDao.findBrandByCode(validated.brandCode())
                .filter(existing -> !existing.getId().equals(validated.id()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã hãng đã tồn tại: " + validated.brandCode());
                });

        applyBrandData(brand, validated);
        Brand savedBrand = carDao.saveBrand(brand);
        if (previousStatus != Status.INACTIVE && savedBrand.getStatus() == Status.INACTIVE) {
            carDao.deactivateCarsByBrandId(savedBrand.getId());
        }
        return toBrandItem(savedBrand);
    }

    @Override
    public void deactivateBrand(Long brandId) {
        if (brandId == null) {
            throw new IllegalArgumentException("Hãng xe không hợp lệ.");
        }
        Brand brand = carDao.findBrandById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hãng xe cần ngừng hoạt động."));
        brand.setStatus(Status.INACTIVE);
        Brand savedBrand = carDao.saveBrand(brand);
        carDao.deactivateCarsByBrandId(savedBrand.getId());
    }

    @Override
    public List<CategoryManagementItem> getCategories(String keyword, Status statusFilter) {
        return carDao.findCategories(keyword, statusFilter).stream()
                .map(this::toCategoryItem)
                .toList();
    }

    @Override
    public CategoryManagementItem createCategory(CategoryCommandRequest request) {
        CategoryCommandRequest validated = validateCategoryRequest(request, false);
        carDao.findCategoryByCode(validated.categoryCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã loại xe đã tồn tại: " + validated.categoryCode());
        });

        CarCategory category = new CarCategory();
        applyCategoryData(category, validated);
        return toCategoryItem(carDao.saveCategory(category));
    }

    @Override
    public CategoryManagementItem updateCategory(CategoryCommandRequest request) {
        CategoryCommandRequest validated = validateCategoryRequest(request, true);
        CarCategory category = carDao.findCategoryById(validated.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại xe cần cập nhật."));
        Status previousStatus = category.getStatus();

        carDao.findCategoryByCode(validated.categoryCode())
                .filter(existing -> !existing.getId().equals(validated.id()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã loại xe đã tồn tại: " + validated.categoryCode());
                });

        applyCategoryData(category, validated);
        CarCategory savedCategory = carDao.saveCategory(category);
        if (previousStatus != Status.INACTIVE && savedCategory.getStatus() == Status.INACTIVE) {
            carDao.deactivateCarsByCategoryId(savedCategory.getId());
        }
        return toCategoryItem(savedCategory);
    }

    @Override
    public void deactivateCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Loại xe không hợp lệ.");
        }
        CarCategory category = carDao.findCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại xe cần ngừng hoạt động."));
        category.setStatus(Status.INACTIVE);
        CarCategory savedCategory = carDao.saveCategory(category);
        carDao.deactivateCarsByCategoryId(savedCategory.getId());
    }

    private void applyData(Car car, CarCommandRequest request) {
        car.setCarCode(request.carCode().trim().toUpperCase());
        car.setCarName(request.carName().trim());

        car.setBrand(carDao.findBrandById(request.brandId())
                .orElseThrow(() -> new IllegalArgumentException("Hãng xe không tồn tại.")));

        CarCategory category = carDao.findCategoryById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Loại xe không tồn tại."));
        car.setCategory(category);

        Branch branch = carDao.findBranchById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không tồn tại."));
        car.setBranch(branch);

        car.setImportPrice(request.importPrice());
        car.setSalePrice(request.salePrice());
        car.setQuantity(request.quantity());
        car.setAvailableQuantity(request.availableQuantity());
        car.setStatus(request.status());
    }

    private CarCommandRequest validateRequest(CarCommandRequest request, boolean requireId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu xe không hợp lệ.");
        }
        if (requireId && request.id() == null) {
            throw new IllegalArgumentException("Thiếu mã định danh xe.");
        }
        if (request.carCode() == null || request.carCode().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã xe.");
        }
        if (request.carName() == null || request.carName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên xe.");
        }
        if (request.brandId() == null || request.categoryId() == null || request.branchId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ hãng, loại và chi nhánh.");
        }
        if (request.importPrice() == null || request.importPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá nhập không hợp lệ.");
        }
        if (request.salePrice() == null || request.salePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bán không hợp lệ.");
        }
        if (request.quantity() == null || request.quantity() < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ.");
        }
        if (request.availableQuantity() == null || request.availableQuantity() < 0 || request.availableQuantity() > request.quantity()) {
            throw new IllegalArgumentException("Tồn kho khả dụng phải từ 0 đến số lượng tổng.");
        }
        if (request.salePrice().compareTo(request.importPrice()) < 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn hoặc bằng giá nhập.");
        }

        Status normalizedStatus = request.status() == null ? Status.ACTIVE : request.status();
        return new CarCommandRequest(
                request.id(),
                request.carCode() == null ? null : request.carCode().trim().toUpperCase(),
                request.carName().trim(),
                request.brandId(),
                request.categoryId(),
                request.branchId(),
                request.importPrice(),
                request.salePrice(),
                request.quantity(),
                request.availableQuantity(),
                normalizedStatus
        );
    }

    private CarManagementItem toCarItem(Car car) {
        return new CarManagementItem(
                car.getId(),
                car.getCarCode(),
                car.getCarName(),
                car.getBrand().getId(),
                car.getBrand().getBrandName(),
                car.getCategory().getId(),
                car.getCategory().getCategoryName(),
                car.getBranch().getId(),
                car.getBranch().getBranchName(),
                car.getImportPrice(),
                car.getSalePrice(),
                car.getQuantity(),
                car.getAvailableQuantity(),
                car.getStatus()
        );
    }

    private void applyBrandData(Brand brand, BrandCommandRequest request) {
        brand.setBrandCode(request.brandCode().trim().toUpperCase());
        brand.setBrandName(request.brandName().trim());
        brand.setCountry(request.country() == null ? null : request.country().trim());
        brand.setStatus(request.status());
    }

    private BrandCommandRequest validateBrandRequest(BrandCommandRequest request, boolean requireId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu hãng xe không hợp lệ.");
        }
        if (requireId && request.id() == null) {
            throw new IllegalArgumentException("Thiếu mã định danh hãng xe.");
        }
        if (request.brandCode() == null || request.brandCode().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã hãng xe.");
        }
        if (request.brandName() == null || request.brandName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên hãng xe.");
        }
        Status normalizedStatus = request.status() == null ? Status.ACTIVE : request.status();
        return new BrandCommandRequest(
                request.id(),
                request.brandCode() == null ? null : request.brandCode().trim().toUpperCase(),
                request.brandName().trim(),
                request.country() == null || request.country().isBlank() ? null : request.country().trim(),
                normalizedStatus
        );
    }

    private BrandManagementItem toBrandItem(Brand brand) {
        return new BrandManagementItem(
                brand.getId(),
                brand.getBrandCode(),
                brand.getBrandName(),
                brand.getCountry(),
                brand.getStatus()
        );
    }

    private void applyCategoryData(CarCategory category, CategoryCommandRequest request) {
        category.setCategoryCode(request.categoryCode().trim().toUpperCase());
        category.setCategoryName(request.categoryName().trim());
        category.setStatus(request.status());
    }

    private CategoryCommandRequest validateCategoryRequest(CategoryCommandRequest request, boolean requireId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu loại xe không hợp lệ.");
        }
        if (requireId && request.id() == null) {
            throw new IllegalArgumentException("Thiếu mã định danh loại xe.");
        }
        if (request.categoryCode() == null || request.categoryCode().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã loại xe.");
        }
        if (request.categoryName() == null || request.categoryName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên loại xe.");
        }
        Status normalizedStatus = request.status() == null ? Status.ACTIVE : request.status();
        return new CategoryCommandRequest(
                request.id(),
                request.categoryCode() == null ? null : request.categoryCode().trim().toUpperCase(),
                request.categoryName().trim(),
                normalizedStatus
        );
    }

    private String nextCarCode() {
        List<String> existingCodes = carDao.findCars(null, null).stream()
                .map(Car::getCarCode)
                .toList();
        return CodeGeneratorUtil.nextCodeFromExisting(existingCodes, "CAR-", 4);
    }

    private String nextBrandCode() {
        List<String> existingCodes = carDao.findBrands(null, null).stream()
                .map(Brand::getBrandCode)
                .toList();
        return CodeGeneratorUtil.nextCodeFromExisting(existingCodes, "BRAND-", 4);
    }

    private String nextCategoryCode() {
        List<String> existingCodes = carDao.findCategories(null, null).stream()
                .map(CarCategory::getCategoryCode)
                .toList();
        return CodeGeneratorUtil.nextCodeFromExisting(existingCodes, "CAT-", 4);
    }

    private CategoryManagementItem toCategoryItem(CarCategory category) {
        return new CategoryManagementItem(
                category.getId(),
                category.getCategoryCode(),
                category.getCategoryName(),
                category.getStatus()
        );
    }
}

