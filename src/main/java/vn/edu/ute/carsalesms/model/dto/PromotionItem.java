package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionItem(
        Long id,
        String promotionCode,
        String promotionName,
        String discountType,
        BigDecimal discountValue,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        Status status
) {
}
