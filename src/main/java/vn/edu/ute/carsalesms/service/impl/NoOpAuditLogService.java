package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.model.dto.AuditLogItem;
import vn.edu.ute.carsalesms.service.AuditLogService;

import java.util.List;

/**
 * No-op logger dung lam mac dinh de khong pha vo constructor cu.
 */
public class NoOpAuditLogService implements AuditLogService {

    @Override
    public void log(String action, String entityName, Long entityId, String oldValue, String newValue) {
        // no-op
    }

    @Override
    public void logByStaffId(Long staffId, String action, String entityName, Long entityId, String oldValue, String newValue) {
        // no-op
    }

    @Override
    public List<AuditLogItem> findLogs(String keyword, String actionFilter, String entityFilter, int limit) {
        return List.of();
    }

    @Override
    public List<String> findActionFilters() {
        return List.of();
    }

    @Override
    public List<String> findEntityFilters() {
        return List.of();
    }
}

