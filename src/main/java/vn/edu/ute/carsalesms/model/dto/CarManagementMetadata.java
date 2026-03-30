package vn.edu.ute.carsalesms.model.dto;

import java.util.List;

public record CarManagementMetadata(
        List<CarLookupItem> brands,
        List<CarLookupItem> categories,
        List<CarLookupItem> branches
) {
    public static CarManagementMetadata empty() {
        return new CarManagementMetadata(List.of(), List.of(), List.of());
    }
}

