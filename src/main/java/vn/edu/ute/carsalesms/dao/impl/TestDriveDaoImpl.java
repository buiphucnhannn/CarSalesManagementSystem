package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.dao.TestDriveDao;
import vn.edu.ute.carsalesms.model.entity.TestDrive;
import vn.edu.ute.carsalesms.config.JpaUtil;

import java.util.List;
import java.util.Optional;

public class TestDriveDaoImpl implements TestDriveDao {

    @Override
    public List<TestDrive> findAll() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            // Join fetch (Tối ưu Lặp N+1) để lấy Tên Khách, Xe và Nhân Viên 
            return em.createQuery(
                "SELECT td FROM TestDrive td JOIN FETCH td.customer JOIN FETCH td.car JOIN FETCH td.staff ORDER BY td.scheduledTime DESC", 
                TestDrive.class).getResultList();
        }
    }

    @Override
    public List<TestDrive> findByKeyword(String keyword) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return findAll();
            }
            String jpql = "SELECT td FROM TestDrive td JOIN FETCH td.customer c JOIN FETCH td.car car JOIN FETCH td.staff " +
                          "WHERE LOWER(td.testDriveCode) LIKE LOWER(:kw) OR LOWER(c.fullName) LIKE LOWER(:kw) " +
                          "OR LOWER(car.modelName) LIKE LOWER(:kw) ORDER BY td.scheduledTime DESC";
            TypedQuery<TestDrive> query = em.createQuery(jpql, TestDrive.class);
            query.setParameter("kw", "%" + keyword.trim() + "%");
            return query.getResultList();
        }
    }

    @Override
    public Optional<TestDrive> findById(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            TestDrive td = em.find(TestDrive.class, id);
            return Optional.ofNullable(td);
        }
    }

    @Override
    public TestDrive save(TestDrive testDrive) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(testDrive);
            em.getTransaction().commit();
            return testDrive;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public TestDrive update(TestDrive testDrive) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            TestDrive updated = em.merge(testDrive);
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
