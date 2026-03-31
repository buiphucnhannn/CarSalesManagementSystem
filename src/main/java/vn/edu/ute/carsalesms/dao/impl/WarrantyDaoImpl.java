package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.dao.WarrantyDao;
import vn.edu.ute.carsalesms.model.entity.Warranty;
import vn.edu.ute.carsalesms.config.JpaUtil;

import java.util.List;
import java.util.Optional;

public class WarrantyDaoImpl implements WarrantyDao {

    @Override
    public List<Warranty> findAll() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            return em.createQuery(
                "SELECT w FROM Warranty w JOIN FETCH w.saleOrderDetail sod JOIN FETCH sod.saleOrder o JOIN FETCH o.customer c JOIN FETCH sod.car car ORDER BY w.endDate DESC", 
                Warranty.class).getResultList();
        }
    }

    @Override
    public List<Warranty> findByKeyword(String keyword) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return findAll();
            }
            String jpql = "SELECT w FROM Warranty w JOIN FETCH w.saleOrderDetail sod JOIN FETCH sod.saleOrder o JOIN FETCH o.customer c JOIN FETCH sod.car car " +
                          "WHERE LOWER(w.warrantyCode) LIKE LOWER(:kw) " +
                          "OR LOWER(c.fullName) LIKE LOWER(:kw) " +
                          "OR LOWER(car.modelName) LIKE LOWER(:kw) " +
                          "OR LOWER(sod.vin) LIKE LOWER(:kw) " +
                          "ORDER BY w.endDate DESC";
            TypedQuery<Warranty> query = em.createQuery(jpql, Warranty.class);
            query.setParameter("kw", "%" + keyword.trim() + "%");
            return query.getResultList();
        }
    }

    @Override
    public Optional<Warranty> findById(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Warranty w = em.find(Warranty.class, id);
            return Optional.ofNullable(w);
        }
    }

    @Override
    public Optional<Warranty> findBySaleOrderDetailId(Long detailId) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            TypedQuery<Warranty> query = em.createQuery(
                "SELECT w FROM Warranty w WHERE w.saleOrderDetail.id = :sodId", Warranty.class);
            query.setParameter("sodId", detailId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Warranty save(Warranty warranty) {
         EntityManager em = JpaUtil.getEntityManager();
         try {
             em.getTransaction().begin();
             em.persist(warranty);
             em.getTransaction().commit();
             return warranty;
         } catch (Exception e) {
             em.getTransaction().rollback();
             throw e;
         } finally {
             em.close();
         }
    }

    @Override
    public Warranty update(Warranty warranty) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Warranty updated = em.merge(warranty);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
