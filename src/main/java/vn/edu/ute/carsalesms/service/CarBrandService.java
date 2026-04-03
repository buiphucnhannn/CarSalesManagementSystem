package vn.edu.ute.carsalesms.service;

import java.util.List;
import vn.edu.ute.carsalesms.model.dto.BrandCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BrandManagementItem;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * Nhom use-case quan ly hang xe.
 */
public interface CarBrandService {

    List<BrandManagementItem> getBrands(String keyword, Status statusFilter);

    BrandManagementItem createBrand(BrandCommandRequest request);

    BrandManagementItem updateBrand(BrandCommandRequest request);

    void deactivateBrand(Long brandId);
}

