package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.AuditLogDao;
import vn.edu.ute.carsalesms.model.entity.AuditLog;

import java.util.List;

public class AuditLogDaoImpl implements AuditLogDao {

    @Override
    public AuditLog save(AuditLog log) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            AuditLog merged = em.merge(log);
            em.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<AuditLog> findLogs(String keyword, String actionFilter, String entityFilter, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String kw = normalizeKeyword(keyword);
            String action = normalizeFilter(actionFilter);
            String entity = normalizeFilter(entityFilter);

            String jpql = "select al from AuditLog al " +
                    "join fetch al.staff s " +
                    "where (:kw is null or lower(s.staffCode) like :kw " +
                    "or lower(s.fullName) like :kw " +
                    "or lower(al.action) like :kw " +
                    "or lower(al.entityName) like :kw " +
                    "or lower(coalesce(al.newValue, '')) like :kw " +
                    "or lower(coalesce(al.oldValue, '')) like :kw) " +
                    "and (:action is null or al.action = :action) " +
                    "and (:entity is null or al.entityName = :entity) " +
                    "order by al.createdAt desc";

            TypedQuery<AuditLog> query = em.createQuery(jpql, AuditLog.class)
                    .setParameter("kw", kw)
                    .setParameter("action", action)
                    .setParameter("entity", entity)
                    .setMaxResults(limit <= 0 ? 500 : limit);

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<String> findDistinctActions() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("select distinct al.action from AuditLog al order by al.action", String.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<String> findDistinctEntities() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("select distinct al.entityName from AuditLog al order by al.entityName", String.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return "%" + keyword.trim().toLowerCase() + "%";
    }

    private String normalizeFilter(String value) {
        if (value == null || value.trim().isEmpty() || "Tất cả".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}

