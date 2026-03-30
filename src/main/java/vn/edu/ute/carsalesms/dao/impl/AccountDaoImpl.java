package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.AccountDao;
import vn.edu.ute.carsalesms.model.entity.Account;

import java.util.Optional;

public class AccountDaoImpl implements AccountDao {

    @Override
    public Optional<Account> findByUsername(String username) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            Account account = entityManager.createQuery(
                            "select a from Account a " +
                                    "join fetch a.staff s " +
                                    "left join fetch s.branch " +
                                    "where lower(a.username) = :username", Account.class)
                    .setParameter("username", username.toLowerCase())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(account);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Account save(Account account) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            Account merged = entityManager.merge(account);
            entityManager.flush();
            // Eagerly initialize lazy associations so they survive after EM closes
            if (merged.getStaff() != null) {
                merged.getStaff().getFullName();            // force init Staff proxy
                if (merged.getStaff().getBranch() != null) {
                    merged.getStaff().getBranch().getBranchName(); // force init Branch proxy
                }
            }
            entityManager.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex;
        } finally {
            entityManager.close();
        }
    }
}

