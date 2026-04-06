package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.InstallmentPlanDao;
import vn.edu.ute.carsalesms.model.entity.InstallmentPlan;

import java.util.List;
import java.util.Optional;

/**
 * Lớp triển khai cho InstallmentPlanDao, quản lý dữ liệu các kỳ hạn trả góp.
 */
public class InstallmentPlanDaoImpl implements InstallmentPlanDao {

    /**
     * Tìm tất cả các kỳ hạn trả góp của một đơn hàng, sắp xếp theo số thứ tự kỳ hạn.
     */
    @Override
    public List<InstallmentPlan> findByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Tải sẵn các thông tin liên quan để hiển thị.
            return em.createQuery("SELECT ip FROM InstallmentPlan ip JOIN FETCH ip.saleOrder o JOIN FETCH o.customer c JOIN FETCH o.staff s JOIN FETCH s.branch b WHERE o.id = :orderId ORDER BY ip.installmentNo ASC", InstallmentPlan.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm một kỳ hạn trả góp theo ID.
     */
    @Override
    public Optional<InstallmentPlan> findById(Long planId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            InstallmentPlan ip = em.createQuery(
                            "SELECT ip FROM InstallmentPlan ip " +
                                    "JOIN FETCH ip.saleOrder o " +
                                    "JOIN FETCH o.staff s " +
                                    "JOIN FETCH s.branch b " +
                                    "WHERE ip.id = :planId", InstallmentPlan.class)
                    .setParameter("planId", planId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(ip);
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật một kỳ hạn trả góp.
     */
    @Override
    public InstallmentPlan update(InstallmentPlan plan) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            InstallmentPlan merged = em.merge(plan);
            em.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    /**
     * Lưu hàng loạt các kỳ hạn trả góp.
     * Thường dùng khi tạo mới một hợp đồng trả góp.
     */
    @Override
    public void saveAll(List<InstallmentPlan> plans) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            for (InstallmentPlan p : plans) {
                em.persist(p); // Dùng persist cho các đối tượng mới.
            }
            em.flush(); // Đẩy các thay đổi vào DB.
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
