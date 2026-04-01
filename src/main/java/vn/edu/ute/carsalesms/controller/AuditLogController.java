package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.AuditLogItem;
import vn.edu.ute.carsalesms.service.AuditLogService;

import java.util.List;
import java.util.Objects;

public class AuditLogController {

	private final AuditLogService auditLogService;

	public AuditLogController(AuditLogService auditLogService) {
		this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
	}

	public List<AuditLogItem> loadLogs(String keyword, String actionFilter, String entityFilter, int limit) {
		return auditLogService.findLogs(keyword, actionFilter, entityFilter, limit);
	}

	public List<String> loadActionFilters() {
		return auditLogService.findActionFilters();
	}

	public List<String> loadEntityFilters() {
		return auditLogService.findEntityFilters();
	}
}

