package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.enums.Status;

public record CarManagementItem(
        Long id,
        String carCode,
        String carName,
        Long brandId,
        String brandName,
        Long categoryId,
        String categoryName,
        Long branchId,
        String branchName,
        BigDecimal importPrice,
        BigDecimal salePrice,
        Integer quantity,
        Integer availableQuantity,
        Status status
) {
}

