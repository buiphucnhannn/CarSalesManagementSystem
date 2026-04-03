package vn.edu.ute.carsalesms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.model.dto.CarCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CarLookupItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementMetadata;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Car;
import vn.edu.ute.carsalesms.model.entity.CarCategory;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.CarInventoryService;
import vn.edu.ute.carsalesms.session.UserSessionContext;
import vn.edu.ute.carsalesms.util.CodeGeneratorUtil;

public class CarInventoryServiceImpl implements CarInventoryService {

    private final CarDao carDao;
    private final UserSessionContext sessionContext;

    public CarInventoryServiceImpl(CarDao carDao, UserSessionContext sessionContext) {
        this.carDao = Objects.requireNonNull(carDao, "carDao is required");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext is required");
    }

    @Override
    public List<CarManagementItem> getCars(String keyword, Status statusFilter) {
        List<CarManagementItem> cars = carDao.findCars(keyword, statusFilter).stream()
                .map(this::toCarItem)
                .toList();
        if (sessionContext.isAdmin()) {
            return cars;
        }
        Long sessionBranchId = sessionContext.currentBranchId();
        if (sessionBranchId == null) {
            return cars;
        }
        return cars.stream().filter(item -> sessionBranchId.equals(item.branchId())).toList();
    }

    @Override
    public CarManagementMetadata getMetadata() {
        List<CarLookupItem> branches = carDao.findActiveBranches().stream()
                .map(branch -> new CarLookupItem(branch.getId(), branch.getBranchCode(), branch.getBranchName()))
                .toList();
        if (!sessionContext.isAdmin()) {
            Long sessionBranchId = sessionContext.currentBranchId();
            if (sessionBranchId != null) {
                branches = branches.stream().filter(branch -> sessionBranchId.equals(branch.id())).toList();
            }
        }

        return new CarManagementMetadata(
                carDao.findActiveBrands().stream()
                        .map(brand -> new CarLookupItem(brand.getId(), brand.getBrandCode(), brand.getBrandName()))
                        .toList(),
                carDao.findActiveCategories().stream()
                        .map(category -> new CarLookupItem(category.getId(), category.getCategoryCode(), category.getCategoryName()))
                        .toList(),
                branches,
                nextCarCode(),
                nextBrandCode(),
                nextCategoryCode()
        );
    }

    @Override
    public CarManagementItem createCar(CarCommandRequest request) {
        CarCommandRequest validated = validateRequest(request, false);
        assertBranchAccess(validated.branchId(), null);
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
        assertBranchAccess(car.getBranch() == null ? null : car.getBranch().getId(),
                car.getBranch() == null ? null : car.getBranch().getBranchName());
        assertBranchAccess(validated.branchId(), null);

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
        assertBranchAccess(car.getBranch() == null ? null : car.getBranch().getId(),
                car.getBranch() == null ? null : car.getBranch().getBranchName());
        car.setStatus(Status.INACTIVE);
        carDao.save(car);
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

    private void assertBranchAccess(Long branchId, String branchName) {
        sessionContext.assertBranchAccess(branchId, branchName);
    }

    private String nextCarCode() {
        List<String> existingCodes = carDao.findCars(null, null).stream().map(Car::getCarCode).toList();
        return CodeGeneratorUtil.nextCodeFromExisting(existingCodes, "CAR-", 4);
    }

    private String nextBrandCode() {
        List<String> existingCodes = carDao.findBrands(null, null).stream()
                .map(vn.edu.ute.carsalesms.model.entity.Brand::getBrandCode)
                .toList();
        return CodeGeneratorUtil.nextCodeFromExisting(existingCodes, "BRAND-", 4);
    }

    private String nextCategoryCode() {
        List<String> existingCodes = carDao.findCategories(null, null).stream().map(CarCategory::getCategoryCode).toList();
        return CodeGeneratorUtil.nextCodeFromExisting(existingCodes, "CAT-", 4);
    }
}

