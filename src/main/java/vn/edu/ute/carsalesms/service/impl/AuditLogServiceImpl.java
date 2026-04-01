package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.AuditLogDao;
import vn.edu.ute.carsalesms.dao.StaffDao;
import vn.edu.ute.carsalesms.model.dto.AuditLogItem;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.entity.AuditLog;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.session.CurrentSession;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuditLogServiceImpl implements AuditLogService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final AuditLogDao auditLogDao;
    private final StaffDao staffDao;

    public AuditLogServiceImpl(AuditLogDao auditLogDao, StaffDao staffDao) {
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao is required");
        this.staffDao = Objects.requireNonNull(staffDao, "staffDao is required");
    }

    @Override
    public void log(String action, String entityName, Long entityId, String oldValue, String newValue) {
        AuthenticatedUser user = CurrentSession.getCurrentUser();
        if (user == null || user.staffId() == null) {
            return;
        }
        logByStaffId(user.staffId(), action, entityName, entityId, oldValue, newValue);
    }

    @Override
    public void logByStaffId(Long staffId, String action, String entityName, Long entityId, String oldValue, String newValue) {
        if (staffId == null || isBlank(action) || isBlank(entityName)) {
            return;
        }

        // Khong de audit fail lam hong nghiep vu chinh.
        try {
            Staff staff = staffDao.findStaffById(staffId).orElse(null);
            if (staff == null) {
                return;
            }

            AuditLog log = new AuditLog();
            log.setStaff(staff);
            log.setAction(action.trim());
            log.setEntityName(entityName.trim());
            log.setEntityId(entityId);
            log.setOldValue(truncate(oldValue));
            log.setNewValue(truncate(newValue));
            log.setCreatedAt(LocalDateTime.now(VIETNAM_ZONE));
            auditLogDao.save(log);
        } catch (Exception ignored) {
            // Ignore by design.
        }
    }

    @Override
    public List<AuditLogItem> findLogs(String keyword, String actionFilter, String entityFilter, int limit) {
        return auditLogDao.findLogs(keyword, actionFilter, entityFilter, limit).stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findActionFilters() {
        return auditLogDao.findDistinctActions();
    }

    @Override
    public List<String> findEntityFilters() {
        return auditLogDao.findDistinctEntities();
    }

    private AuditLogItem mapToItem(AuditLog log) {
        Staff staff = log.getStaff();
        return new AuditLogItem(
                log.getId(),
                log.getCreatedAt(),
                staff != null ? staff.getId() : null,
                staff != null ? staff.getStaffCode() : "",
                staff != null ? staff.getFullName() : "",
                staff != null && staff.getRole() != null ? staff.getRole().name() : "",
                log.getAction(),
                log.getEntityName(),
                log.getEntityId(),
                log.getOldValue(),
                log.getNewValue()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        int max = 4000;
        return value.length() <= max ? value : value.substring(0, max);
    }
}

