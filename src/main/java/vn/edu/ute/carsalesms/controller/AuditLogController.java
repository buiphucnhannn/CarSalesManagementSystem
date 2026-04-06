package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.AuditLogItem;
import vn.edu.ute.carsalesms.service.AuditLogService;

import java.util.List;
import java.util.Objects;

/**
 * AuditLogController xử lý các yêu cầu liên quan đến nhật ký kiểm toán.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic nhật ký kiểm toán.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào giao diện AuditLogService
 * thay vì một triển khai cụ thể.
 */
public class AuditLogController {

	private final AuditLogService auditLogService;

	/**
	 * Xây dựng một AuditLogController mới với AuditLogService đã cho.
	 * @param auditLogService dịch vụ sẽ được sử dụng để quản lý nhật ký kiểm toán.
	 */
	public AuditLogController(AuditLogService auditLogService) {
		this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
	}

	/**
	 * Tải nhật ký khớp với các bộ lọc đã cho.
	 * @param keyword từ khóa để tìm kiếm.
	 * @param actionFilter bộ lọc hành động.
	 * @param entityFilter bộ lọc thực thể.
	 * @param limit số lượng nhật ký tối đa để tải.
	 * @return danh sách các mục nhật ký kiểm toán.
	 */
	public List<AuditLogItem> loadLogs(String keyword, String actionFilter, String entityFilter, int limit) {
		return auditLogService.findLogs(keyword, actionFilter, entityFilter, limit);
	}

	/**
	 * Tải tất cả các bộ lọc hành động có sẵn.
	 * @return danh sách các bộ lọc hành động.
	 */
	public List<String> loadActionFilters() {
		return auditLogService.findActionFilters();
	}

	/**
	 * Tải tất cả các bộ lọc thực thể có sẵn.
	 * @return danh sách các bộ lọc thực thể.
	 */
	public List<String> loadEntityFilters() {
		return auditLogService.findEntityFilters();
	}
}
