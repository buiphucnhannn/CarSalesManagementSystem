package vn.edu.ute.carsalesms.model.dto;

import java.time.LocalDateTime;

public record TestDriveRequest(
        Long customerId,
        Long carId,
        Long staffId,
        LocalDateTime scheduledTime,
        String note
) {
}
