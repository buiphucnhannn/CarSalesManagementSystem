package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.AuditLogDao;
import vn.edu.ute.carsalesms.model.entity.AuditLog;

import java.util.List;

/**
 * Lớp triển khai cho AuditLogDao, sử dụng JPA để lưu và truy vấn dữ liệu nhật ký kiểm toán.
 */
public class AuditLogDaoImpl implements AuditLogDao {

    /**
     * Lưu một bản ghi nhật ký mới.
     */
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

    /**
     * Tìm kiếm và lọc các bản ghi nhật ký.
     */
    @Override
    public List<AuditLog> findLogs(String keyword, String actionFilter, String entityFilter, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Chuẩn hóa các giá trị đầu vào để truy vấn.
            String kw = normalizeKeyword(keyword);
            String action = normalizeFilter(actionFilter);
            String entity = normalizeFilter(entityFilter);

            // Xây dựng câu truy vấn JPQL để tìm kiếm trên nhiều trường.
            String jpql = "select al from AuditLog al " +
                    "join fetch al.staff s " + // Tải thông tin nhân viên liên quan
                    "where (:kw is null or lower(s.staffCode) like :kw " +
                    "or lower(s.fullName) like :kw " +
                    "or lower(al.action) like :kw " +
                    "or lower(al.entityName) like :kw " +
                    "or lower(coalesce(al.newValue, '')) like :kw " + // coalesce để tránh lỗi với giá trị null
                    "or lower(coalesce(al.oldValue, '')) like :kw) " +
                    "and (:action is null or al.action = :action) " + // Lọc theo hành động
                    "and (:entity is null or al.entityName = :entity) " + // Lọc theo thực thể
                    "order by al.createdAt desc"; // Sắp xếp theo thời gian gần nhất

            TypedQuery<AuditLog> query = em.createQuery(jpql, AuditLog.class)
                    .setParameter("kw", kw)
                    .setParameter("action", action)
                    .setParameter("entity", entity)
                    .setMaxResults(limit <= 0 ? 500 : limit); // Giới hạn số lượng kết quả trả về

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách các hành động (action) duy nhất từ nhật ký.
     */
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

    /**
     * Lấy danh sách các thực thể (entity) duy nhất từ nhật ký.
     */
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

    /**
     * Chuẩn hóa từ khóa tìm kiếm: chuyển thành chữ thường và thêm ký tự đại diện '%'.
     * @param keyword Từ khóa thô.
     * @return Từ khóa đã được chuẩn hóa, hoặc null nếu không có từ khóa.
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return "%" + keyword.trim().toLowerCase() + "%";
    }

    /**
     * Chuẩn hóa giá trị bộ lọc: loại bỏ khoảng trắng thừa và coi "Tất cả" là không lọc.
     * @param value Giá trị bộ lọc thô.
     * @return Giá trị đã được chuẩn hóa, hoặc null nếu không cần lọc.
     */
    private String normalizeFilter(String value) {
        if (value == null || value.trim().isEmpty() || "Tất cả".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
