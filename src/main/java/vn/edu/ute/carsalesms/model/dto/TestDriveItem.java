package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import java.time.LocalDateTime;

public record TestDriveItem(
        Long id,
        String testDriveCode,
        String customerName,
        String carModel,
        String staffName,
        LocalDateTime scheduledTime,
        String result,
        TestDriveStatus status,
        String note
) {
}
