package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;
import vn.edu.ute.carsalesms.model.enums.Status;

public record CarCommandRequest(
        Long id,
        String carCode,
        String carName,
        Long brandId,
        Long categoryId,
        Long branchId,
        BigDecimal importPrice,
        BigDecimal salePrice,
        Integer quantity,
        Integer availableQuantity,
        Status status
) {
}

