package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.PromotionDao;
import vn.edu.ute.carsalesms.model.entity.Promotion;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lớp triển khai cho PromotionDao, sử dụng JPA/Hibernate để thao tác với dữ liệu khuyến mãi.
 */
public class PromotionDaoImpl implements PromotionDao {

    /**
     * Lấy danh sách các chương trình khuyến mãi đang có hiệu lực tại ngày hiện tại.
     * Điều kiện để một khuyến mãi có hiệu lực là:
     * - Trạng thái (status) phải là ACTIVE.
     * - Ngày hiện tại (today) phải nằm trong khoảng từ ngày bắt đầu (startDate) đến ngày kết thúc (endDate).
     */
    @Override
    public List<Promotion> findActivePromotions() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            return em.createQuery(
                            "select p from Promotion p " +
                            "where p.status = :status " +
                            "and p.startDate <= :today " +
                            "and p.endDate >= :today " +
                            "order by p.promotionName", Promotion.class)
                    .setParameter("status", Status.ACTIVE)
                    .setParameter("today", today)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm một khuyến mãi theo ID.
     */
    @Override
    public Optional<Promotion> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Promotion.class, id));
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả các khuyến mãi đã tạo, không phân biệt trạng thái hay ngày hết hạn.
     */
    @Override
    public List<Promotion> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Promotion p ORDER BY p.createdAt DESC", Promotion.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lưu một khuyến mãi mới.
     */
    @Override
    public Promotion save(Promotion promotion) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(promotion); // Dùng persist cho việc tạo mới.
            em.getTransaction().commit();
            return promotion;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật một khuyến mãi đã có.
     */
    @Override
    public Promotion update(Promotion promotion) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Promotion merged = em.merge(promotion); // Dùng merge cho việc cập nhật.
            em.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
