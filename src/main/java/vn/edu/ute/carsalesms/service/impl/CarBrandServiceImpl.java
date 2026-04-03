package vn.edu.ute.carsalesms.service.impl;

import java.util.List;
import java.util.Objects;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.model.dto.BrandCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BrandManagementItem;
import vn.edu.ute.carsalesms.model.entity.Brand;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.CarBrandService;
import vn.edu.ute.carsalesms.session.UserSessionContext;

public class CarBrandServiceImpl implements CarBrandService {

    private final CarDao carDao;
    private final UserSessionContext sessionContext;

    public CarBrandServiceImpl(CarDao carDao, UserSessionContext sessionContext) {
        this.carDao = Objects.requireNonNull(carDao, "carDao is required");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext is required");
    }

    @Override
    public List<BrandManagementItem> getBrands(String keyword, Status statusFilter) {
        return carDao.findBrands(keyword, statusFilter).stream().map(this::toBrandItem).toList();
    }

    @Override
    public BrandManagementItem createBrand(BrandCommandRequest request) {
        assertAdminOnly();
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
        assertAdminOnly();
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
        assertAdminOnly();
        if (brandId == null) {
            throw new IllegalArgumentException("Hãng xe không hợp lệ.");
        }
        Brand brand = carDao.findBrandById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hãng xe cần ngừng hoạt động."));
        brand.setStatus(Status.INACTIVE);
        Brand savedBrand = carDao.saveBrand(brand);
        carDao.deactivateCarsByBrandId(savedBrand.getId());
    }

    private void assertAdminOnly() {
        if (!sessionContext.isAuthenticated() || !sessionContext.isAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới có quyền thao tác dữ liệu dùng chung toàn hệ thống.");
        }
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
                request.brandCode().trim().toUpperCase(),
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
}

