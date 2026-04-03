package vn.edu.ute.carsalesms.service.impl;

import java.util.List;
import java.util.Objects;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.model.dto.CategoryCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CategoryManagementItem;
import vn.edu.ute.carsalesms.model.entity.CarCategory;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.CarCategoryService;
import vn.edu.ute.carsalesms.session.UserSessionContext;

public class CarCategoryServiceImpl implements CarCategoryService {

    private final CarDao carDao;
    private final UserSessionContext sessionContext;

    public CarCategoryServiceImpl(CarDao carDao, UserSessionContext sessionContext) {
        this.carDao = Objects.requireNonNull(carDao, "carDao is required");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext is required");
    }

    @Override
    public List<CategoryManagementItem> getCategories(String keyword, Status statusFilter) {
        return carDao.findCategories(keyword, statusFilter).stream().map(this::toCategoryItem).toList();
    }

    @Override
    public CategoryManagementItem createCategory(CategoryCommandRequest request) {
        assertAdminOnly();
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
        assertAdminOnly();
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
        assertAdminOnly();
        if (categoryId == null) {
            throw new IllegalArgumentException("Loại xe không hợp lệ.");
        }
        CarCategory category = carDao.findCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại xe cần ngừng hoạt động."));
        category.setStatus(Status.INACTIVE);
        CarCategory savedCategory = carDao.saveCategory(category);
        carDao.deactivateCarsByCategoryId(savedCategory.getId());
    }

    private void assertAdminOnly() {
        if (!sessionContext.isAuthenticated() || !sessionContext.isAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới có quyền thao tác dữ liệu dùng chung toàn hệ thống.");
        }
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
                request.categoryCode().trim().toUpperCase(),
                request.categoryName().trim(),
                normalizedStatus
        );
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

