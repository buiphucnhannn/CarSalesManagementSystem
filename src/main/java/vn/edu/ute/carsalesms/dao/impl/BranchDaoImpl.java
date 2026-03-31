package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.BranchDao;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BranchDaoImpl implements BranchDao {

    @Override
    public List<Branch> findBranches(String keyword, Status statusFilter) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("select b from Branch b where 1=1");
            if (keyword != null && !keyword.isBlank()) {
                jpql.append(" and (lower(b.branchCode) like :kw"
                        + " or lower(b.branchName) like :kw"
                        + " or lower(b.address) like :kw"
                        + " or lower(b.phone) like :kw"
                        + " or lower(b.email) like :kw)");
            }
            if (statusFilter != null) {
                jpql.append(" and b.status = :status");
            }
            jpql.append(" order by b.updatedAt desc, b.id desc");

            TypedQuery<Branch> query = em.createQuery(jpql.toString(), Branch.class);
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
    public Optional<Branch> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Branch.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Branch> findByCode(String branchCode) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Branch branch = em.createQuery(
                            "select b from Branch b where lower(b.branchCode) = :code", Branch.class)
                    .setParameter("code", branchCode.trim().toLowerCase())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(branch);
        } finally {
            em.close();
        }
    }

    @Override
    public Branch save(Branch branch) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Branch merged = em.merge(branch);
            em.flush();
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
    public long countActiveStaffByBranchId(Long branchId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select count(s.id) from Staff s where s.branch.id = :branchId and s.status = :status",
                            Long.class)
                    .setParameter("branchId", branchId)
                    .setParameter("status", Status.ACTIVE)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long countActiveCarsByBranchId(Long branchId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select count(c.id) from Car c where c.branch.id = :branchId and c.status = :status",
                            Long.class)
                    .setParameter("branchId", branchId)
                    .setParameter("status", Status.ACTIVE)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> findBranchSalesReportRows(LocalDateTime fromInclusive,
                                                    LocalDateTime toExclusive,
                                                    Status statusFilter) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select b.id, b.branchCode, b.branchName, b.status, "
                            + "coalesce(sum(case when o.orderDate >= :from and o.orderDate < :to then 1 else 0 end), 0), "
                            + "coalesce(sum(case when o.orderDate >= :from and o.orderDate < :to and o.orderStatus = :paid then 1 else 0 end), 0), "
                            + "coalesce(sum(case when o.orderDate >= :from and o.orderDate < :to and o.orderStatus in :pendingStatuses then 1 else 0 end), 0), "
                            + "coalesce(sum(case when o.orderDate >= :from and o.orderDate < :to and o.orderStatus = :cancelled then 1 else 0 end), 0), "
                            + "coalesce(sum(case when o.orderDate >= :from and o.orderDate < :to and o.orderStatus = :paid then o.finalAmount else 0 end), 0), "
                            + "max(case when o.orderDate >= :from and o.orderDate < :to then o.orderDate else null end) "
                            + "from Branch b "
                            + "left join b.staffs s "
                            + "left join s.saleOrders o "
                            + "where 1=1");

            if (statusFilter != null) {
                jpql.append(" and b.status = :status");
            }

            jpql.append(" group by b.id, b.branchCode, b.branchName, b.status order by b.branchName asc");

            TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive)
                    .setParameter("paid", OrderStatus.PAID)
                    .setParameter("pendingStatuses", List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED))
                    .setParameter("cancelled", OrderStatus.CANCELLED);

            if (statusFilter != null) {
                query.setParameter("status", statusFilter);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

