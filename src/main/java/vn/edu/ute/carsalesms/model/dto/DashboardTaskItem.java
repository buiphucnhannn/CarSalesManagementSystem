package vn.edu.ute.carsalesms.model.dto;

import java.time.LocalDateTime;

public record DashboardTaskItem(
        String action,
        String customerName,
        LocalDateTime dueAt,
        String status
) {
}

