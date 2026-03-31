package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;
import java.time.LocalDate;

public record WarrantyItem(
        Long id,
        String warrantyCode,
        String saleOrderCode,
        String customerName,
        String carModel,
        LocalDate startDate,
        LocalDate endDate,
        WarrantyStatus warrantyStatus,
        String note
) {
}
