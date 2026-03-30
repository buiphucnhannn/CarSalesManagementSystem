package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.InstallmentPlanDao;
import vn.edu.ute.carsalesms.model.entity.InstallmentPlan;

import java.util.List;
import java.util.Optional;

public class InstallmentPlanDaoImpl implements InstallmentPlanDao {

    @Override
    public List<InstallmentPlan> findByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT ip FROM InstallmentPlan ip JOIN FETCH ip.saleOrder o JOIN FETCH o.customer c WHERE o.id = :orderId ORDER BY ip.installmentNo ASC", InstallmentPlan.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<InstallmentPlan> findById(Long planId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            InstallmentPlan ip = em.find(InstallmentPlan.class, planId);
            return Optional.ofNullable(ip);
        } finally {
            em.close();
        }
    }

    @Override
    public InstallmentPlan update(InstallmentPlan plan) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            InstallmentPlan merged = em.merge(plan);
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
    public void saveAll(List<InstallmentPlan> plans) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            for (InstallmentPlan p : plans) {
                em.persist(p);
            }
            em.flush();
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
