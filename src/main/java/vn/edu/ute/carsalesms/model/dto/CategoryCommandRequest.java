package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

public record CategoryCommandRequest(
        Long id,
        String categoryCode,
        String categoryName,
        Status status
) {
}

