package vn.edu.ute.carsalesms.model.dto;

import java.time.LocalDateTime;

/**
 * DTO hien thi nhat ky thao tac tren UI.
 */
public record AuditLogItem(
        Long id,
        LocalDateTime createdAt,
        Long staffId,
        String staffCode,
        String staffName,
        String staffRole,
        String action,
        String entityName,
        Long entityId,
        String oldValue,
        String newValue
) {
}

