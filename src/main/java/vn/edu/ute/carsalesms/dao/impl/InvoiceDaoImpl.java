package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.InvoiceDao;
import vn.edu.ute.carsalesms.model.entity.Invoice;

import java.util.Optional;
import java.util.List;

/**
 * Triển khai InvoiceDao dùng JPA/Hibernate.
 */
public class InvoiceDaoImpl implements InvoiceDao {

    @Override
    public Optional<Invoice> findByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Invoice inv = em.createQuery(
                            "select i from Invoice i " +
                            "join fetch i.saleOrder o " +
                            "where o.id = :orderId", Invoice.class)
                    .setParameter("orderId", orderId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(inv);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu hóa đơn mới trong transaction.
     */
    @Override
    public Invoice save(Invoice invoice) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Invoice merged = em.merge(invoice);
            em.flush();
            // Eager-init proxy
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

    @Override
    public List<Invoice> findAll(String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String qlString = "SELECT i FROM Invoice i JOIN FETCH i.saleOrder o JOIN FETCH o.customer c " +
                              "WHERE :kw IS NULL OR LOWER(i.invoiceCode) LIKE :kw " +
                              "OR LOWER(o.orderCode) LIKE :kw OR LOWER(c.fullName) LIKE :kw " +
                              "ORDER BY i.issuedDate DESC";
            
            String searchKw = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim().toLowerCase() + "%" : null;
            
            return em.createQuery(qlString, Invoice.class)
                    .setParameter("kw", searchKw)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
