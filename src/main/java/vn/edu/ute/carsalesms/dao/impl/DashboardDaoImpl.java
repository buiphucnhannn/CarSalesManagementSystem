package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.DashboardDao;
import vn.edu.ute.carsalesms.model.dto.AdminRecentOrderItem;
import vn.edu.ute.carsalesms.model.dto.DashboardTaskItem;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardDaoImpl implements DashboardDao {

    @Override
    public long countOrdersByStaffAndStatuses(Long staffId, List<OrderStatus> statuses) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select count(so.id) from SaleOrder so " +
                                    "where so.staff.id = :staffId and so.orderStatus in :statuses", Long.class)
                    .setParameter("staffId", staffId)
                    .setParameter("statuses", statuses)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public BigDecimal sumCompletedPaymentsByStaffInRange(Long staffId, LocalDateTime from, LocalDateTime to) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select coalesce(sum(p.amount), 0) from Payment p " +
                                    "where p.saleOrder.staff.id = :staffId " +
                                    "and p.paymentStatus = :status " +
                                    "and p.paymentDate >= :from and p.paymentDate < :to", BigDecimal.class)
                    .setParameter("staffId", staffId)
                    .setParameter("status", PaymentStatus.COMPLETED)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long countTestDrivesByStaffAndStatusInRange(Long staffId, TestDriveStatus status, LocalDateTime from, LocalDateTime to) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select count(td.id) from TestDrive td " +
                                    "where td.staff.id = :staffId and td.status = :status " +
                                    "and td.scheduledTime >= :from and td.scheduledTime < :to", Long.class)
                    .setParameter("staffId", staffId)
                    .setParameter("status", status)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long countWarrantiesByStaffAndStatus(Long staffId, WarrantyStatus status) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select count(w.id) from Warranty w " +
                                    "join w.saleOrderDetail d " +
                                    "join d.saleOrder so " +
                                    "where so.staff.id = :staffId and w.warrantyStatus = :status", Long.class)
                    .setParameter("staffId", staffId)
                    .setParameter("status", status)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<DashboardTaskItem> findOrderTasksByStaff(Long staffId, int limit) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select so.orderCode, c.fullName, so.orderDate, so.orderStatus " +
                                    "from SaleOrder so " +
                                    "join so.customer c " +
                                    "where so.staff.id = :staffId and so.orderStatus in :statuses " +
                                    "order by so.orderDate asc", Object[].class)
                    .setParameter("staffId", staffId)
                    .setParameter("statuses", List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED))
                    .setMaxResults(limit)
                    .getResultStream()
                    .map(row -> new DashboardTaskItem(
                            "Đơn " + row[0],
                            (String) row[1],
                            (LocalDateTime) row[2],
                            ((OrderStatus) row[3]).name()
                    ))
                    .toList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<DashboardTaskItem> findUpcomingTestDriveTasksByStaff(Long staffId, LocalDateTime from, LocalDateTime to, int limit) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select car.carName, c.fullName, td.scheduledTime, td.status " +
                                    "from TestDrive td " +
                                    "join td.customer c " +
                                    "join td.car car " +
                                    "where td.staff.id = :staffId and td.status = :status and td.scheduledTime >= :from and td.scheduledTime < :to " +
                                    "order by td.scheduledTime asc", Object[].class)
                    .setParameter("staffId", staffId)
                    .setParameter("status", TestDriveStatus.SCHEDULED)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .setMaxResults(limit)
                    .getResultStream()
                    .map(row -> new DashboardTaskItem(
                            "Lái thử " + row[0],
                            (String) row[1],
                            (LocalDateTime) row[2],
                            ((TestDriveStatus) row[3]).name()
                    ))
                    .toList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public BigDecimal sumCompletedPaymentsInRange(LocalDateTime from, LocalDateTime to) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select coalesce(sum(p.amount), 0) from Payment p " +
                                    "where p.paymentStatus = :status and p.paymentDate >= :from and p.paymentDate < :to", BigDecimal.class)
                    .setParameter("status", PaymentStatus.COMPLETED)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long countOrdersInRange(LocalDateTime from, LocalDateTime to) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select count(so.id) from SaleOrder so where so.orderDate >= :from and so.orderDate < :to", Long.class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long countOrdersByStatuses(List<OrderStatus> statuses) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select count(so.id) from SaleOrder so where so.orderStatus in :statuses", Long.class)
                    .setParameter("statuses", statuses)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long countTestDrivesByStatusInRange(TestDriveStatus status, LocalDateTime from, LocalDateTime to) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select count(td.id) from TestDrive td " +
                                    "where td.status = :status and td.scheduledTime >= :from and td.scheduledTime < :to", Long.class)
                    .setParameter("status", status)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<AdminRecentOrderItem> findRecentOrders(int limit) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select so.orderCode, c.fullName, coalesce(min(car.carName), 'N/A'), so.orderStatus " +
                                    "from SaleOrder so " +
                                    "join so.customer c " +
                                    "left join so.saleOrderDetails d " +
                                    "left join d.car car " +
                                    "group by so.id, so.orderCode, c.fullName, so.orderStatus, so.orderDate " +
                                    "order by so.orderDate desc", Object[].class)
                    .setMaxResults(limit)
                    .getResultStream()
                    .map(row -> new AdminRecentOrderItem(
                            (String) row[0],
                            (String) row[1],
                            (String) row[2],
                            ((OrderStatus) row[3]).name()
                    ))
                    .toList();
        } finally {
            entityManager.close();
        }
    }
}

