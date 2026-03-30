package vn.edu.ute.carsalesms.model.dto;

public record AdminRecentOrderItem(
        String orderCode,
        String customerName,
        String carName,
        String status
) {
}

