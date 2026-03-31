package vn.edu.ute.carsalesms.model.dto;

import java.util.List;

public record CarManagementMetadata(
        List<CarLookupItem> brands,
        List<CarLookupItem> categories,
        List<CarLookupItem> branches,
        String nextCarCode,
        String nextBrandCode,
        String nextCategoryCode
) {
    public static CarManagementMetadata empty() {
        return new CarManagementMetadata(List.of(), List.of(), List.of(), "CAR-0001", "BRAND-0001", "CAT-0001");
    }
}

