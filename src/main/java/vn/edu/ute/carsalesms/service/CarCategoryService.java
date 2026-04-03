package vn.edu.ute.carsalesms.service;

import java.util.List;
import vn.edu.ute.carsalesms.model.dto.CategoryCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CategoryManagementItem;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * Nhom use-case quan ly loai xe.
 */
public interface CarCategoryService {

    List<CategoryManagementItem> getCategories(String keyword, Status statusFilter);

    CategoryManagementItem createCategory(CategoryCommandRequest request);

    CategoryManagementItem updateCategory(CategoryCommandRequest request);

    void deactivateCategory(Long categoryId);
}

