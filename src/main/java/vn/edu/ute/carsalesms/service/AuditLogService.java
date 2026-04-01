package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.AuditLogItem;

import java.util.List;

public interface AuditLogService {

    void log(String action, String entityName, Long entityId, String oldValue, String newValue);

    void logByStaffId(Long staffId, String action, String entityName, Long entityId, String oldValue, String newValue);

    List<AuditLogItem> findLogs(String keyword, String actionFilter, String entityFilter, int limit);

    List<String> findActionFilters();

    List<String> findEntityFilters();
}

