package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.StaffDao;
import vn.edu.ute.carsalesms.model.entity.Account;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.util.List;
import java.util.Optional;

/**
 * Triển khai StaffDao dùng JPA/Hibernate.
 * Các query dùng join fetch để tránh LazyInitializationException sau khi EntityManager đóng.
 */
public class StaffDaoImpl implements StaffDao {

    // ─── Staff ───────────────────────────────────────────────────────────

    /**
     * Tìm nhân viên kèm thông tin branch và account (eager fetch).
     * Dùng LEFT JOIN FETCH account để phát hiện nhân viên chưa có tài khoản.
     */
    @Override
    public List<Staff> findStaffs(String keyword, Status statusFilter) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select distinct s from Staff s " +
                    "join fetch s.branch b " +
                    "left join fetch s.account a " +
                    "where 1=1");

            if (keyword != null && !keyword.isBlank()) {
                jpql.append(
                        " and (lower(s.staffCode) like :kw" +
                        " or lower(s.fullName) like :kw" +
                        " or lower(s.email) like :kw)");
            }
            if (statusFilter != null) {
                jpql.append(" and s.status = :status");
            }
            jpql.append(" order by s.updatedAt desc, s.id desc");

            TypedQuery<Staff> query = em.createQuery(jpql.toString(), Staff.class);
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
    public Optional<Staff> findStaffById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Staff staff = em.createQuery(
                            "select s from Staff s " +
                            "join fetch s.branch " +
                            "left join fetch s.account a " +
                            "where s.id = :id", Staff.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(staff);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Staff> findStaffByCode(String staffCode) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Staff result = em.createQuery(
                            "select s from Staff s where lower(s.staffCode) = :code",
                            Staff.class)
                    .setParameter("code", staffCode.trim().toLowerCase())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(result);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu nhân viên, sau đó eager-init các lazy proxy trước khi EM đóng.
     */
    @Override
    public Staff saveStaff(Staff staff) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Staff merged = em.merge(staff);
            em.flush();
            // Khởi tạo proxy để tránh LazyInitializationException sau khi EM đóng
            merged.getBranch().getBranchName();
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

    // ─── Branch lookup ────────────────────────────────────────────────────

    @Override
    public List<Branch> findActiveBranches() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select b from Branch b where b.status = :status order by b.branchName",
                            Branch.class)
                    .setParameter("status", Status.ACTIVE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Branch> findBranchById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Branch.class, id));
        } finally {
            em.close();
        }
    }

    // ─── Account ─────────────────────────────────────────────────────────

    /**
     * Tìm tài khoản kèm thông tin nhân viên.
     * Tìm theo username, mã nhân viên, hoặc tên nhân viên.
     */
    @Override
    public List<Account> findAccounts(String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select a from Account a " +
                    "join fetch a.staff s " +
                    "join fetch s.branch " +
                    "where 1=1");

            if (keyword != null && !keyword.isBlank()) {
                jpql.append(
                        " and (lower(a.username) like :kw" +
                        " or lower(s.staffCode) like :kw" +
                        " or lower(s.fullName) like :kw)");
            }
            jpql.append(" order by a.updatedAt desc, a.id desc");

            TypedQuery<Account> query = em.createQuery(jpql.toString(), Account.class);
            if (keyword != null && !keyword.isBlank()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Account> findAccountById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Account account = em.createQuery(
                            "select a from Account a " +
                            "join fetch a.staff s " +
                            "join fetch s.branch " +
                            "where a.id = :id", Account.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(account);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Account> findAccountByUsername(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Account result = em.createQuery(
                            "select a from Account a " +
                            "join fetch a.staff s " +
                            "where lower(a.username) = :username", Account.class)
                    .setParameter("username", username.trim().toLowerCase())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(result);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu tài khoản và eager-init proxy trước khi EM đóng.
     */
    @Override
    public Account saveAccount(Account account) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Account merged = em.merge(account);
            em.flush();
            // Khởi tạo các lazy proxy
            merged.getStaff().getFullName();
            merged.getStaff().getBranch().getBranchName();
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
    public void deleteAccountById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Account ref = em.getReference(Account.class, id);
            em.remove(ref);
            em.flush();
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}
