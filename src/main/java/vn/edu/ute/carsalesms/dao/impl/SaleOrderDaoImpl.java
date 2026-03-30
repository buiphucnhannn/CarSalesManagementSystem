package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.SaleOrderDao;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.entity.SaleOrderDetail;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai SaleOrderDao dùng JPA/Hibernate.
 * Dùng join fetch để tránh LazyInitializationException sau khi EM đóng.
 */
public class SaleOrderDaoImpl implements SaleOrderDao {

    /**
     * Tìm danh sách đơn bán với eager-fetch customer, staff, promotion.
     * Kết quả sắp xếp theo order_date DESC.
     */
    @Override
    public List<SaleOrder> findOrders(String keyword, OrderStatus statusFilter) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select distinct o from SaleOrder o " +
                    "join fetch o.customer c " +
                    "join fetch o.staff s " +
                    "left join fetch o.promotion p " +
                    "where 1=1");

            if (keyword != null && !keyword.isBlank()) {
                jpql.append(" and (lower(o.orderCode) like :kw" +
                            " or lower(c.fullName) like :kw" +
                            " or lower(s.fullName) like :kw)");
            }
            if (statusFilter != null) {
                jpql.append(" and o.orderStatus = :status");
            }
            jpql.append(" order by o.orderDate desc");

            TypedQuery<SaleOrder> query = em.createQuery(jpql.toString(), SaleOrder.class);
            if (keyword != null && !keyword.isBlank()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (statusFilter != null) {
                query.setParameter("status", statusFilter);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<SaleOrder> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            SaleOrder order = em.createQuery(
                            "select o from SaleOrder o " +
                            "join fetch o.customer " +
                            "join fetch o.staff " +
                            "left join fetch o.promotion " +
                            "where o.id = :id", SaleOrder.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(order);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByCode(String orderCode) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "select count(o) from SaleOrder o where o.orderCode = :code",
                            Long.class)
                    .setParameter("code", orderCode)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lưu đơn bán trong transaction, eager-init proxy trước khi EM đóng.
     */
    @Override
    public SaleOrder save(SaleOrder order) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            SaleOrder merged = em.merge(order);
            em.flush();
            // Eager-init các lazy proxy
            merged.getCustomer().getFullName();
            merged.getStaff().getFullName();
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
    public List<SaleOrderDetail> findDetailsByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select d from SaleOrderDetail d " +
                            "join fetch d.car c " +
                            "join fetch c.brand " +
                            "where d.saleOrder.id = :orderId", SaleOrderDetail.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public SaleOrderDetail saveDetail(SaleOrderDetail detail) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            SaleOrderDetail merged = em.merge(detail);
            em.flush();
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
