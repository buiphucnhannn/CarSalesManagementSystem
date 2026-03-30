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
 * Triển khai PromotionDao dùng JPA/Hibernate.
 */
public class PromotionDaoImpl implements PromotionDao {

    /**
     * Lấy danh sách khuyến mãi còn hiệu lực hôm nay:
     *   status = ACTIVE AND startDate <= today AND endDate >= today
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

    @Override
    public Optional<Promotion> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Promotion.class, id));
        } finally {
            em.close();
        }
    }

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

    @Override
    public Promotion save(Promotion promotion) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(promotion);
            em.getTransaction().commit();
            return promotion;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Promotion update(Promotion promotion) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Promotion merged = em.merge(promotion);
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
