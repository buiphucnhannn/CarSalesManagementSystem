package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

public record BrandCommandRequest(
        Long id,
        String brandCode,
        String brandName,
        String country,
        Status status
) {
}

