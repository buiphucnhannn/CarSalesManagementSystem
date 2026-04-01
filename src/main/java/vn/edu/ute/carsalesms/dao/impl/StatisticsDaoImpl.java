package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.StatisticsDao;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class StatisticsDaoImpl implements StatisticsDao {

    @Override
    public BigDecimal sumRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select coalesce(sum(p.amount), 0) from Payment p " +
                    "where p.paymentStatus = :status and p.paymentDate >= :from and p.paymentDate < :to" +
                    (staffId != null ? " and p.saleOrder.staff.id = :staffId" : "");
            TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class)
                    .setParameter("status", PaymentStatus.COMPLETED)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive);
            if (staffId != null) {
                query.setParameter("staffId", staffId);
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long countOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select count(so.id) from SaleOrder so " +
                    "where so.orderDate >= :from and so.orderDate < :to" +
                    (staffId != null ? " and so.staff.id = :staffId" : "");
            TypedQuery<Long> query = em.createQuery(jpql, Long.class)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive);
            if (staffId != null) {
                query.setParameter("staffId", staffId);
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long countPaidOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select count(so.id) from SaleOrder so " +
                    "where so.orderStatus = :paidStatus and so.orderDate >= :from and so.orderDate < :to" +
                    (staffId != null ? " and so.staff.id = :staffId" : "");
            TypedQuery<Long> query = em.createQuery(jpql, Long.class)
                    .setParameter("paidStatus", OrderStatus.PAID)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive);
            if (staffId != null) {
                query.setParameter("staffId", staffId);
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findDailyRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select function('date', p.paymentDate), coalesce(sum(p.amount), 0) " +
                    "from Payment p " +
                    "where p.paymentStatus = :status and p.paymentDate >= :from and p.paymentDate < :to" +
                    (staffId != null ? " and p.saleOrder.staff.id = :staffId " : " ") +
                    "group by function('date', p.paymentDate) " +
                    "order by function('date', p.paymentDate)";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class)
                    .setParameter("status", PaymentStatus.COMPLETED)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive);
            if (staffId != null) {
                query.setParameter("staffId", staffId);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findDailyOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select function('date', so.orderDate), count(so.id) " +
                    "from SaleOrder so " +
                    "where so.orderDate >= :from and so.orderDate < :to" +
                    (staffId != null ? " and so.staff.id = :staffId " : " ") +
                    "group by function('date', so.orderDate) " +
                    "order by function('date', so.orderDate)";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive);
            if (staffId != null) {
                query.setParameter("staffId", staffId);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findOrderStatusBreakdown(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select so.orderStatus, count(so.id), coalesce(sum(so.finalAmount), 0) " +
                    "from SaleOrder so " +
                    "where so.orderDate >= :from and so.orderDate < :to" +
                    (staffId != null ? " and so.staff.id = :staffId " : " ") +
                    "group by so.orderStatus";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive);
            if (staffId != null) {
                query.setParameter("staffId", staffId);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findPaymentMethodBreakdown(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select p.paymentMethod, count(p.id), coalesce(sum(p.amount), 0) " +
                    "from Payment p " +
                    "where p.paymentStatus = :status and p.paymentDate >= :from and p.paymentDate < :to" +
                    (staffId != null ? " and p.saleOrder.staff.id = :staffId " : " ") +
                    "group by p.paymentMethod";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class)
                    .setParameter("status", PaymentStatus.COMPLETED)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive);
            if (staffId != null) {
                query.setParameter("staffId", staffId);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findTopCars(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select c.carCode, c.carName, coalesce(sum(d.quantity), 0), coalesce(sum(d.lineTotal), 0) " +
                    "from SaleOrderDetail d " +
                    "join d.saleOrder so " +
                    "join d.car c " +
                    "where so.orderStatus = :paidStatus and so.orderDate >= :from and so.orderDate < :to" +
                    (staffId != null ? " and so.staff.id = :staffId " : " ") +
                    "group by c.id, c.carCode, c.carName " +
                    "order by coalesce(sum(d.quantity), 0) desc";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class)
                    .setParameter("paidStatus", OrderStatus.PAID)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive)
                    .setMaxResults(limit);
            if (staffId != null) {
                query.setParameter("staffId", staffId);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findBranchRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "select b.branchCode, b.branchName, count(distinct so.id), coalesce(sum(p.amount), 0) " +
                    "from Payment p " +
                    "join p.saleOrder so " +
                    "join so.staff s " +
                    "join s.branch b " +
                    "where p.paymentStatus = :status and p.paymentDate >= :from and p.paymentDate < :to " +
                    "group by b.id, b.branchCode, b.branchName " +
                    "order by coalesce(sum(p.amount), 0) desc";
            return em.createQuery(jpql, Object[].class)
                    .setParameter("status", PaymentStatus.COMPLETED)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}

