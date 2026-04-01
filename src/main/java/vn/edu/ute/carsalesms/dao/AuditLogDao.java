package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.AuditLog;

import java.util.List;

public interface AuditLogDao {

    AuditLog save(AuditLog log);

    List<AuditLog> findLogs(String keyword, String actionFilter, String entityFilter, int limit);

    List<String> findDistinctActions();

    List<String> findDistinctEntities();
}

