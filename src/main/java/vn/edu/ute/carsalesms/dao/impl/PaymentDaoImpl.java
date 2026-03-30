package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.PaymentDao;
import vn.edu.ute.carsalesms.model.entity.Payment;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai PaymentDao dùng JPA/Hibernate.
 */
public class PaymentDaoImpl implements PaymentDao {

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select p from Payment p " +
                            "join fetch p.saleOrder o " +
                            "where o.id = :orderId " +
                            "order by p.paymentDate desc", Payment.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tính tổng tiền COMPLETED dùng JPQL aggregate.
     * Trả về BigDecimal.ZERO nếu chưa có payment nào COMPLETED.
     */
    @Override
    public BigDecimal sumCompletedByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            BigDecimal result = em.createQuery(
                            "select coalesce(sum(p.amount), 0) " +
                            "from Payment p " +
                            "where p.saleOrder.id = :orderId " +
                            "and p.paymentStatus = :status", BigDecimal.class)
                    .setParameter("orderId", orderId)
                    .setParameter("status", PaymentStatus.COMPLETED)
                    .getSingleResult();
            return result == null ? BigDecimal.ZERO : result;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Payment p = em.createQuery(
                            "select p from Payment p " +
                            "join fetch p.saleOrder o " +
                            "where p.id = :id", Payment.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(p);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu Payment trong transaction.
     */
    @Override
    public Payment save(Payment payment) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Payment merged = em.merge(payment);
            em.flush();
            // Eager-init proxy SaleOrder trước khi EM đóng
            merged.getSaleOrder().getOrderCode();
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
